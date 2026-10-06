package probabilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "ui.tests", matches = "true")
class ProbabilityCalculatorGuiTest {
    @TempDir Path dataDirectory;

    @Test void exercisesEveryAnalysisBothLanguagesHistoryAndSmallWindowLayout() throws Exception {
        String oldDirectory = System.getProperty("probabilitycalculator.dataDir");
        System.setProperty("probabilitycalculator.dataDir", dataDirectory.toString());
        AtomicReference<ProbabilityCalculatorGUI> holder = new AtomicReference<>();
        Path screenshots = Path.of("target", "ui-review");
        Files.createDirectories(screenshots);
        try {
            SwingUtilities.invokeAndWait(() -> {
                ProbabilityCalculatorGUI frame = new ProbabilityCalculatorGUI();
                frame.setSize(980, 720);
                frame.setVisible(true);
                holder.set(frame);
            });
            ProbabilityCalculatorGUI frame = holder.get();
            JComboBox<?> analyses = field(frame, "analysisSelector");
            JComboBox<?> languages = field(frame, "languageSelector");
            for (int language = 0; language < 2; language++) {
                int selectedLanguage = language;
                SwingUtilities.invokeAndWait(() -> languages.setSelectedIndex(selectedLanguage));
                for (int type = 0; type < analyses.getItemCount(); type++) {
                    int selectedType = type;
                    SwingUtilities.invokeAndWait(() -> {
                        analyses.setSelectedIndex(selectedType);
                        frame.getRootPane().getDefaultButton().doClick();
                    });
                    awaitCalculation(frame);
                    SwingUtilities.invokeAndWait(() -> {
                        JTextArea output = field(frame, "output");
                        assertFalse(output.getText().isBlank(), "Missing result for analysis " + selectedType);
                        capture(frame, screenshots.resolve(selectedLanguage + "-" + selectedType + "-980.png"));
                        assertWrappedFieldsFit(frame.getContentPane());
                        frame.setSize(1280, 900);
                        frame.validate();
                        capture(frame, screenshots.resolve(selectedLanguage + "-" + selectedType + "-1280.png"));
                        frame.setSize(980, 720);
                        frame.validate();
                    });
                }
            }
            SwingUtilities.invokeAndWait(() -> {
                analyses.setSelectedIndex(0);
                Map<String, JTextField> inputs = field(frame, "activeFields");
                inputs.get("a").setText("0,75");
                languages.setSelectedIndex(0);
                Map<String, JTextField> translatedInputs = field(frame, "activeFields");
                assertEquals("0,75", translatedInputs.get("a").getText());
                frame.getRootPane().getDefaultButton().doClick();
            });
            awaitCalculation(frame);
            SwingUtilities.invokeAndWait(() -> {
                JTabbedPane tabs = field(frame, "resultTabs");
                tabs.setSelectedIndex(1);
                JTable history = field(frame, "historyTable");
                assertEquals(15, history.getRowCount());
                history.setRowSelectionInterval(0, 0);
                capture(frame, screenshots.resolve("history-980.png"));
                findButton(tabs.getComponentAt(1), "Erneut laden").doClick();
            });
            awaitCalculation(frame);
            SwingUtilities.invokeAndWait(() -> {
                JTable history = field(frame, "historyTable");
                assertEquals(15, history.getRowCount(), "Restoring must not duplicate entries");
                JTextArea output = field(frame, "output");
                assertTrue(output.getText().contains("0,75000000"));
            });
            SwingUtilities.invokeAndWait(frame::dispose);
            java.util.concurrent.ExecutorService writer = field(frame, "historyWriter");
            assertTrue(writer.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));
            assertEquals(15, new HistoryStore(dataDirectory.resolve("history.xml")).load().entries().size());
        } finally {
            if (holder.get() != null) SwingUtilities.invokeAndWait(holder.get()::dispose);
            if (oldDirectory == null) System.clearProperty("probabilitycalculator.dataDir");
            else System.setProperty("probabilitycalculator.dataDir", oldDirectory);
        }
    }

    private void awaitCalculation(ProbabilityCalculatorGUI frame) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            AtomicReference<Object> worker = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> worker.set(field(frame, "calculationWorker")));
            if (worker.get() == null) return;
            Thread.sleep(25);
        }
        fail("Calculation did not finish");
    }

    private void assertWrappedFieldsFit(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JTextArea area && !area.isEditable() && !(area.getParent() instanceof JViewport) && area.isShowing()) {
                try {
                    var last = area.modelToView2D(area.getDocument().getLength());
                    if (last != null) assertTrue(last.getMaxY() <= area.getHeight(), "Clipped text: " + area.getText() + " bounds=" + area.getBounds() + " last=" + last);
                } catch (javax.swing.text.BadLocationException exception) { throw new AssertionError(exception); }
            }
            if (child instanceof Container container) assertWrappedFieldsFit(container);
        }
    }

    private void capture(ProbabilityCalculatorGUI frame, Path file) {
        BufferedImage image = new BufferedImage(frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        frame.getContentPane().paint(graphics);
        graphics.dispose();
        try { ImageIO.write(image, "png", file.toFile()); }
        catch (java.io.IOException exception) { throw new AssertionError(exception); }
    }

    private JButton findButton(Component component, String text) {
        if (component instanceof JButton button && text.equals(button.getText())) return button;
        if (component instanceof Container parent) for (Component child : parent.getComponents()) {
            JButton button = findButton(child, text);
            if (button != null) return button;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T field(Object object, String name) {
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (ReflectiveOperationException exception) { throw new AssertionError(exception); }
    }
}
