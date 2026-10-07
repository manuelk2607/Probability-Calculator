"""Build native installers on their target OS, including a tested Java runtime."""

import argparse
import os
import platform
from pathlib import Path
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]


def run(*args):
    subprocess.run([str(arg) for arg in args], cwd=ROOT, check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--type", choices=["all", "dmg", "msi", "exe", "deb", "app-image"], default="all")
    args = parser.parse_args()
    version = ET.parse(ROOT / "pom.xml").getroot().find("{http://maven.apache.org/POM/4.0.0}version").text
    jar = ROOT / "target" / f"Wahrscheinlichkeitsrechner-{version}.jar"
    if not jar.is_file():
        parser.error("Run mvn package before packaging.")
    system = platform.system()
    allowed = {"Darwin": ["dmg"], "Windows": ["msi", "exe"], "Linux": ["deb"]}
    types = allowed[system] if args.type == "all" else [args.type]
    if any(kind not in allowed[system] + ["app-image"] for kind in types):
        parser.error("This installer type must be built on its target operating system.")
    jpackage = shutil.which("jpackage")
    if not jpackage:
        parser.error("A JDK with jpackage (Java 17+) is required.")
    destination = ROOT / "dist"
    destination.mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="probability-package-") as temporary:
        temporary = Path(temporary)
        inputs = temporary / "input"
        inputs.mkdir()
        shutil.copy2(jar, inputs / jar.name)
        images = temporary / "images"
        run(jpackage, "--type", "app-image", "--name", "Wahrscheinlichkeitsrechner",
            "--input", inputs, "--main-jar", jar.name,
            "--main-class", "probabilities.ProbabilityCalculatorGUI", "--dest", images,
            "--app-version", version, "--vendor", "manuelk2607",
            "--description", "Probability Calculator / Wahrscheinlichkeitsrechner",
            "--jlink-options", "--strip-debug --no-header-files --no-man-pages",
            "--java-options", "-Dfile.encoding=UTF-8")
        app = next(images.iterdir())
        java_name = "java.exe" if system == "Windows" else "java"
        runtime_java = next(path for path in app.rglob(java_name) if path.parent.name == "bin")
        bundled_jar = next(app.rglob(jar.name))
        run(runtime_java, "--version")
        smoke_classes = ROOT / "target" / "test-classes"
        if not (smoke_classes / "probabilities" / "PackagedAppSmoke.class").is_file():
            parser.error("Smoke-test classes are missing. Build without -Dmaven.test.skip=true.")
        run(runtime_java, "-Djava.awt.headless=true", "-cp", os.pathsep.join([str(bundled_jar), str(smoke_classes)]),
            "probabilities.PackagedAppSmoke")
        for kind in types:
            if kind == "app-image":
                shutil.copytree(app, destination / app.name, symlinks=True)
                continue
            output = temporary / kind
            options = []
            if system == "Windows":
                options = ["--win-per-user-install", "--win-menu", "--win-shortcut", "--win-dir-chooser",
                           "--win-upgrade-uuid", "644f2daa-61bc-49ca-b60b-fc5723fdb05b"]
            elif system == "Linux":
                options = ["--linux-package-name", "probability-calculator", "--linux-app-category", "Science",
                           "--linux-shortcut", "--linux-menu-group", "Education;Science"]
            run(jpackage, "--type", kind, "--app-image", app, "--name", "Wahrscheinlichkeitsrechner",
                "--dest", output, "--app-version", version, "--vendor", "manuelk2607", *options)
            installer = next(output.glob(f"*.{kind}"))
            name = f"Probability-Calculator-{version}-{system.lower()}-{platform.machine()}.{kind}"
            shutil.copy2(installer, destination / name)
            print(f"Created {destination / name}")
    shutil.copy2(jar, destination / jar.name)


if __name__ == "__main__":
    main()
