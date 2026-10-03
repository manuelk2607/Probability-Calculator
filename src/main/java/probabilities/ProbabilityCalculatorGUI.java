package probabilities;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProbabilityCalculatorGUI extends JFrame {
    private static final Color WINDOW_BG = new Color(232, 232, 232);
    private static final Color PANEL_BG = new Color(244, 244, 244);
    private static final Color FIELD_BG = Color.WHITE;
    private static final Color BORDER = new Color(150, 150, 150);
    private static final Color BUTTON_BLUE = new Color(214, 224, 239);
    private static final Color GRAPH_BLUE = new Color(75, 126, 190);
    private static final Color GRAPH_RED = new Color(195, 92, 82);
    private static final Color GRAPH_GREEN = new Color(90, 153, 106);
    private static final Color GRAPH_YELLOW = new Color(213, 169, 70);

    private final JComboBox<AnalysisType> analysisSelector = new JComboBox<>(AnalysisType.values());
    private final JPanel inputHost = new JPanel(new BorderLayout());
    private final JTextArea output = new JTextArea(14, 38);
    private final ChartPanel chartPanel = new ChartPanel();
    private final JLabel statusLabel = new JLabel("Bereit");

    private JTextField probAField;
    private JTextField probBField;
    private JTextField intersectionField;
    private JTextField likelihoodsField;
    private JTextField priorsField;
    private JTextField selectedIndexField;

    public ProbabilityCalculatorGUI() {
        super("Wahrscheinlichkeitsrechner");
        configureLookAndFeel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(940, 640));
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(WINDOW_BG);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        analysisSelector.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                rebuildInputPanel();
            }
        });
        rebuildInputPanel();

        pack();
        setLocationRelativeTo(null);
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing falls back to the default look and feel.
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        header.setBackground(PANEL_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Wahrscheinlichkeitsrechner");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        header.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        header.add(new JLabel("Test family"), gbc);

        analysisSelector.setPreferredSize(new Dimension(270, 26));
        gbc.gridx = 1;
        header.add(analysisSelector, gbc);

        gbc.gridx = 2;
        header.add(new JLabel("Input mode"), gbc);

        JLabel modeLabel = new JLabel("Probability values [0, 1]");
        modeLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        modeLabel.setOpaque(true);
        modeLabel.setBackground(Color.WHITE);
        modeLabel.setPreferredSize(new Dimension(190, 24));
        gbc.gridx = 3;
        header.add(modeLabel, gbc);

        gbc.gridx = 4;
        gbc.weightx = 1.0;
        header.add(new JLabel(), gbc);

        return header;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        main.setBackground(WINDOW_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        inputHost.setPreferredSize(new Dimension(340, 520));
        main.add(inputHost, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        main.add(createOutputArea(), gbc);

        return main;
    }

    private JPanel createOutputArea() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WINDOW_BG);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        output.setMargin(new Insets(8, 8, 8, 8));

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(PANEL_BG);
        resultPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), "Output parameters"));
        resultPanel.add(new JScrollPane(output), BorderLayout.CENTER);

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBackground(PANEL_BG);
        graphPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), "Probability plot"));
        graphPanel.add(chartPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.42;
        panel.add(resultPanel, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy = 1;
        gbc.weighty = 0.58;
        panel.add(graphPanel, gbc);

        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        footer.setBackground(PANEL_BG);
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        footer.add(statusLabel, BorderLayout.WEST);
        return footer;
    }

    private void rebuildInputPanel() {
        inputHost.removeAll();
        inputHost.add(createInputPanel((AnalysisType) analysisSelector.getSelectedItem()), BorderLayout.CENTER);
        inputHost.revalidate();
        inputHost.repaint();
        output.setText("");
        chartPanel.clear("Noch keine Berechnung ausgeführt.");
        statusLabel.setText("Bereit");
    }

    private JPanel createInputPanel(AnalysisType type) {
        FormPanel form = new FormPanel();
        form.addSection("Analysis");
        form.addReadOnly("Selected procedure", type.displayName);
        form.addReadOnly("Tail(s)", type.tailDescription);
        form.addSeparator();

        switch (type) {
            case COMPLEMENT -> createComplementInputs(form);
            case JOINT -> createJointInputs(form);
            case CONDITIONAL -> createConditionalInputs(form);
            case BAYES -> createBayesInputs(form);
        }

        form.addBottomGlue();
        return form;
    }

    private void createComplementInputs(FormPanel form) {
        form.addSection("Input parameters");
        probAField = form.addField("Pr(A)", "0.30");
        probBField = form.addField("Pr(B)", "0.40");
        form.addActionRow(
                actionButton("Calculate", this::calculateComplement),
                actionButton("Clear", this::clearCurrent)
        );
    }

    private void createJointInputs(FormPanel form) {
        form.addSection("Input parameters");
        probAField = form.addField("Pr(A)", "0.30");
        probBField = form.addField("Pr(B)", "0.40");
        intersectionField = form.addField("Pr(A and B)", "");
        form.addHint("Leave Pr(A and B) empty to assume independence.");
        form.addActionRow(
                actionButton("Calculate", this::calculateJoint),
                actionButton("Clear", this::clearCurrent)
        );
    }

    private void createConditionalInputs(FormPanel form) {
        form.addSection("Input parameters");
        intersectionField = form.addField("Pr(A and B)", "0.12");
        probAField = form.addField("Pr(A)", "0.30");
        probBField = form.addField("Pr(B)", "0.40");
        form.addActionRow(
                actionButton("Calculate", this::calculateConditional),
                actionButton("Clear", this::clearCurrent)
        );
    }

    private void createBayesInputs(FormPanel form) {
        form.addSection("Input parameters");
        likelihoodsField = form.addField("Pr(B|A_i)", "0.90; 0.20");
        priorsField = form.addField("Pr(A_i)", "0.30; 0.70");
        selectedIndexField = form.addField("Requested A_i", "1");
        form.addHint("Separate list values with semicolon or space. Pr(A_i) must sum to 1.");
        form.addActionRow(
                actionButton("Calculate", this::calculateBayes),
                actionButton("Clear", this::clearCurrent)
        );
    }

    private JButton actionButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setBackground(BUTTON_BLUE);
        button.addActionListener(_event -> {
            try {
                action.run();
                statusLabel.setText("Berechnung erfolgreich");
            } catch (IllegalArgumentException exception) {
                statusLabel.setText("Eingabe prüfen");
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Eingabe prüfen", JOptionPane.WARNING_MESSAGE);
            }
        });
        return button;
    }

    private void calculateComplement() {
        double a = readProbability(probAField, "Pr(A)");
        double b = readProbability(probBField, "Pr(B)");
        double notA = BaseProbabilities.getProbNotA(a);
        double notB = BaseProbabilities.getProbNotB(b);

        showResults(List.of(
                new ResultLine("Pr(A)", a),
                new ResultLine("Pr(not A)", notA),
                new ResultLine("Pr(B)", b),
                new ResultLine("Pr(not B)", notB)
        ), "Komplementwahrscheinlichkeiten");

        chartPanel.setStackedBars(List.of(
                new BarGroup("A", List.of(
                        new Segment("Pr(A)", a, GRAPH_BLUE),
                        new Segment("Pr(not A)", notA, GRAPH_RED)
                )),
                new BarGroup("B", List.of(
                        new Segment("Pr(B)", b, GRAPH_GREEN),
                        new Segment("Pr(not B)", notB, GRAPH_YELLOW)
                ))
        ), "Komplement-Zerlegung");
    }

    private void calculateJoint() {
        double a = readProbability(probAField, "Pr(A)");
        double b = readProbability(probBField, "Pr(B)");
        double intersection = intersectionField.getText().isBlank()
                ? JointProbs.getProbAAndB(a, b)
                : readProbability(intersectionField, "Pr(A and B)");
        double aOnly = JointProbs.getProbAMinusB(a, intersection);
        double bOnly = JointProbs.getProbBMinusA(b, intersection);
        double neither = 1.0 - JointProbs.getProbAOrB(a, b, intersection);
        ProbabilityUtils.requireProbability(neither, "Pr(neither A nor B)");

        showResults(List.of(
                new ResultLine("Pr(A and B)", intersection),
                new ResultLine("Pr(A or B)", JointProbs.getProbAOrB(a, b, intersection)),
                new ResultLine("Pr(A without B)", aOnly),
                new ResultLine("Pr(B without A)", bOnly),
                new ResultLine("Pr(neither)", neither)
        ), intersectionField.getText().isBlank()
                ? "Schnitt und Vereinigung (Unabhängigkeit angenommen)"
                : "Schnitt und Vereinigung");

        chartPanel.setSegments(List.of(
                new Segment("A only", aOnly, GRAPH_BLUE),
                new Segment("A and B", intersection, GRAPH_GREEN),
                new Segment("B only", bOnly, GRAPH_RED),
                new Segment("Neither", neither, new Color(165, 165, 165))
        ), "Vierfelder-Zerlegung des Ergebnisraums");
    }

    private void calculateConditional() {
        double intersection = readProbability(intersectionField, "Pr(A and B)");
        double a = readProbability(probAField, "Pr(A)");
        double b = readProbability(probBField, "Pr(B)");
        double aGivenB = ConditionalProbs.getProbAGivenB(intersection, b);
        double bGivenA = ConditionalProbs.getProbBGivenA(intersection, a);

        showResults(List.of(
                new ResultLine("Pr(A|B)", aGivenB),
                new ResultLine("Pr(B|A)", bGivenA),
                new ResultLine("Pr(A and B)", intersection)
        ), "Bedingte Wahrscheinlichkeiten");

        chartPanel.setStackedBars(List.of(
                new BarGroup("Given B", List.of(
                        new Segment("Pr(A|B)", aGivenB, GRAPH_BLUE),
                        new Segment("Pr(not A|B)", 1.0 - aGivenB, GRAPH_RED)
                )),
                new BarGroup("Given A", List.of(
                        new Segment("Pr(B|A)", bGivenA, GRAPH_GREEN),
                        new Segment("Pr(not B|A)", 1.0 - bGivenA, GRAPH_YELLOW)
                ))
        ), "Bedingte Anteile im jeweiligen Grundraum");
    }

    private void calculateBayes() {
        List<Double> likelihoods = parseList(likelihoodsField.getText(), "Pr(B|A_i)");
        List<Double> priors = parseList(priorsField.getText(), "Pr(A_i)");
        int index = parseIndex(selectedIndexField.getText(), likelihoods.size()) - 1;
        double total = TotalBayesProbs.totalProbability(likelihoods, priors);
        double posterior = TotalBayesProbs.calcBayes(likelihoods.get(index), priors.get(index), total);

        List<ResultLine> lines = new ArrayList<>();
        lines.add(new ResultLine("Pr(B)", total));
        lines.add(new ResultLine("Pr(A" + (index + 1) + "|B)", posterior));
        for (int i = 0; i < likelihoods.size(); i++) {
            lines.add(new ResultLine("Contribution A" + (i + 1), likelihoods.get(i) * priors.get(i)));
        }
        showResults(lines, "Bayes und totale Wahrscheinlichkeit");

        List<Segment> segments = new ArrayList<>();
        Color[] colors = {GRAPH_BLUE, GRAPH_RED, GRAPH_GREEN, GRAPH_YELLOW, new Color(129, 102, 168), new Color(92, 145, 160)};
        for (int i = 0; i < likelihoods.size(); i++) {
            segments.add(new Segment("A" + (i + 1), likelihoods.get(i) * priors.get(i), colors[i % colors.length]));
        }
        chartPanel.setSegments(segments, "Beiträge zu Pr(B) = Summe Pr(B|A_i) * Pr(A_i)");
    }

    private void clearCurrent() {
        output.setText("");
        chartPanel.clear("Noch keine Berechnung ausgeführt.");
        statusLabel.setText("Bereit");
    }

    private void showResults(List<ResultLine> lines, String heading) {
        StringBuilder builder = new StringBuilder();
        builder.append(heading).append(System.lineSeparator());
        builder.append("=".repeat(Math.max(heading.length(), 12))).append(System.lineSeparator()).append(System.lineSeparator());
        for (ResultLine line : lines) {
            builder.append(String.format("%-24s %s%n", line.label(), format(line.value())));
        }
        output.setText(builder.toString());
    }

    private double readProbability(JTextField field, String name) {
        try {
            return ProbabilityUtils.requireProbability(Double.parseDouble(field.getText().trim().replace(',', '.')), name);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " muss eine Zahl sein.");
        }
    }

    private List<Double> parseList(String raw, String name) {
        try {
            List<Double> values = Arrays.stream(raw.trim().split("[;\\s]+"))
                    .filter(value -> !value.isBlank())
                    .map(value -> ProbabilityUtils.requireProbability(Double.parseDouble(value.replace(',', '.')), name))
                    .toList();
            if (values.isEmpty()) {
                throw new IllegalArgumentException(name + " darf nicht leer sein.");
            }
            return values;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " enthält eine ungültige Zahl.");
        }
    }

    private int parseIndex(String raw, int max) {
        try {
            int index = Integer.parseInt(raw.trim());
            if (index < 1 || index > max) {
                throw new IllegalArgumentException("Das gesuchte A_i muss zwischen 1 und " + max + " liegen.");
            }
            return index;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Das gesuchte A_i muss eine ganze Zahl sein.");
        }
    }

    private String format(double value) {
        return "%.6f   %.2f%%".formatted(value, value * 100.0);
    }

    private enum AnalysisType {
        COMPLEMENT("Exact: Complements", "two sided", "Komplemente"),
        JOINT("Exact: Joint probability", "two sided", "Schnitt & Oder"),
        CONDITIONAL("Exact: Conditional probability", "one sided", "Bedingt"),
        BAYES("Bayes: Total probability", "posterior", "Bayes");

        private final String displayName;
        private final String tailDescription;
        private final String comboLabel;

        AnalysisType(String displayName, String tailDescription, String comboLabel) {
            this.displayName = displayName;
            this.tailDescription = tailDescription;
            this.comboLabel = comboLabel;
        }

        @Override
        public String toString() {
            return comboLabel;
        }
    }

    private record ResultLine(String label, double value) {
    }

    private record Segment(String label, double value, Color color) {
    }

    private record BarGroup(String label, List<Segment> segments) {
    }

    private static final class FormPanel extends JPanel {
        private final GridBagConstraints constraints = new GridBagConstraints();
        private int row = 0;

        private FormPanel() {
            super(new GridBagLayout());
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), "Input parameters"));
            constraints.insets = new Insets(4, 8, 4, 8);
            constraints.fill = GridBagConstraints.HORIZONTAL;
        }

        private void addSection(String text) {
            JLabel label = new JLabel(text);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            constraints.gridx = 0;
            constraints.gridy = row++;
            constraints.gridwidth = 2;
            constraints.weightx = 1.0;
            add(label, constraints);
            constraints.gridwidth = 1;
        }

        private void addSeparator() {
            constraints.gridx = 0;
            constraints.gridy = row++;
            constraints.gridwidth = 2;
            constraints.insets = new Insets(8, 8, 8, 8);
            add(new JSeparator(SwingConstants.HORIZONTAL), constraints);
            constraints.insets = new Insets(4, 8, 4, 8);
            constraints.gridwidth = 1;
        }

        private JTextField addField(String label, String value) {
            JTextField field = new JTextField(value, 12);
            field.setBackground(FIELD_BG);
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.weightx = 0.0;
            add(new JLabel(label), constraints);
            constraints.gridx = 1;
            constraints.weightx = 1.0;
            add(field, constraints);
            row++;
            return field;
        }

        private void addReadOnly(String label, String value) {
            JLabel valueLabel = new JLabel(value);
            valueLabel.setOpaque(true);
            valueLabel.setBackground(Color.WHITE);
            valueLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.weightx = 0.0;
            add(new JLabel(label), constraints);
            constraints.gridx = 1;
            constraints.weightx = 1.0;
            add(valueLabel, constraints);
            row++;
        }

        private void addHint(String text) {
            JTextArea hint = new JTextArea(text);
            hint.setEditable(false);
            hint.setLineWrap(true);
            hint.setWrapStyleWord(true);
            hint.setOpaque(false);
            hint.setFont(hint.getFont().deriveFont(12f));
            constraints.gridx = 0;
            constraints.gridy = row++;
            constraints.gridwidth = 2;
            constraints.weightx = 1.0;
            add(hint, constraints);
            constraints.gridwidth = 1;
        }

        private void addActionRow(JButton primary, JButton secondary) {
            JPanel actions = new JPanel(new GridBagLayout());
            actions.setOpaque(false);
            GridBagConstraints actionConstraints = new GridBagConstraints();
            actionConstraints.insets = new Insets(0, 0, 0, 6);
            actionConstraints.fill = GridBagConstraints.HORIZONTAL;
            actionConstraints.gridx = 0;
            actionConstraints.weightx = 1.0;
            actions.add(primary, actionConstraints);
            actionConstraints.insets = new Insets(0, 6, 0, 0);
            actionConstraints.gridx = 1;
            actions.add(secondary, actionConstraints);

            constraints.gridx = 0;
            constraints.gridy = row++;
            constraints.gridwidth = 2;
            constraints.weightx = 1.0;
            constraints.insets = new Insets(12, 8, 4, 8);
            add(actions, constraints);
            constraints.insets = new Insets(4, 8, 4, 8);
            constraints.gridwidth = 1;
        }

        private void addBottomGlue() {
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.gridwidth = 2;
            constraints.weighty = 1.0;
            add(new JLabel(), constraints);
        }
    }

    private static final class ChartPanel extends JPanel {
        private enum Mode {
            EMPTY,
            SEGMENTS,
            STACKED_BARS
        }

        private Mode mode = Mode.EMPTY;
        private String title = "Noch keine Berechnung ausgeführt.";
        private List<Segment> segments = List.of();
        private List<BarGroup> barGroups = List.of();

        private ChartPanel() {
            setPreferredSize(new Dimension(540, 280));
            setBackground(Color.WHITE);
            clear(title);
        }

        private void clear(String message) {
            mode = Mode.EMPTY;
            title = message;
            segments = List.of();
            barGroups = List.of();
            repaint();
        }

        private void setSegments(List<Segment> segments, String title) {
            this.mode = Mode.SEGMENTS;
            this.title = title;
            this.segments = segments;
            this.barGroups = List.of();
            repaint();
        }

        private void setStackedBars(List<BarGroup> barGroups, String title) {
            this.mode = Mode.STACKED_BARS;
            this.title = title;
            this.barGroups = barGroups;
            this.segments = List.of();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBackground(g);
            paintTitle(g);

            if (mode == Mode.EMPTY) {
                paintEmpty(g);
            } else if (mode == Mode.SEGMENTS) {
                paintSegmentBar(g, segments, 62, getHeight() / 2 - 16, getWidth() - 124, 34);
                paintLegend(g, segments, 62, getHeight() / 2 + 42);
            } else {
                paintStackedBars(g);
            }

            g.dispose();
        }

        private void paintBackground(Graphics2D g) {
            g.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(238, 241, 246)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(190, 190, 190));
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }

        private void paintTitle(Graphics2D g) {
            g.setColor(new Color(30, 30, 30));
            g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
            g.drawString(title, 22, 28);
        }

        private void paintEmpty(Graphics2D g) {
            g.setColor(new Color(105, 105, 105));
            g.setFont(g.getFont().deriveFont(13f));
            g.drawString("Diagramme erscheinen nach der Berechnung.", 42, getHeight() / 2);
        }

        private void paintStackedBars(Graphics2D g) {
            int barX = 96;
            int barWidth = getWidth() - 160;
            int y = 72;
            List<Segment> legendSegments = new ArrayList<>();
            for (BarGroup group : barGroups) {
                g.setColor(new Color(45, 45, 45));
                g.drawString(group.label(), 24, y + 22);
                paintSegmentBar(g, group.segments(), barX, y, barWidth, 28);
                legendSegments.addAll(group.segments());
                y += 54;
            }
            paintAxis(g, barX, y - 18, barWidth);
            paintLegend(g, legendSegments, 24, y + 18);
        }

        private void paintSegmentBar(Graphics2D g, List<Segment> values, int x, int y, int width, int height) {
            g.setStroke(new BasicStroke(1f));
            int currentX = x;
            for (int i = 0; i < values.size(); i++) {
                Segment segment = values.get(i);
                int segmentWidth = i == values.size() - 1
                        ? x + width - currentX
                        : (int) Math.round(width * segment.value());
                g.setColor(segment.color());
                g.fillRect(currentX, y, Math.max(0, segmentWidth), height);
                g.setColor(Color.WHITE);
                if (segmentWidth > 58) {
                    g.drawString("%.1f%%".formatted(segment.value() * 100.0), currentX + 6, y + height - 9);
                }
                currentX += segmentWidth;
            }
            g.setColor(new Color(80, 80, 80));
            g.drawRect(x, y, width, height);
            paintAxis(g, x, y + height + 8, width);
        }

        private void paintAxis(Graphics2D g, int x, int y, int width) {
            g.setColor(new Color(120, 120, 120));
            g.drawLine(x, y, x + width, y);
            g.setFont(g.getFont().deriveFont(11f));
            for (int i = 0; i <= 4; i++) {
                int tickX = x + (width * i / 4);
                g.drawLine(tickX, y - 3, tickX, y + 3);
                g.drawString((i * 25) + "%", tickX - 10, y + 17);
            }
        }

        private void paintLegend(Graphics2D g, List<Segment> rawSegments, int x, int y) {
            List<Segment> unique = new ArrayList<>();
            for (Segment segment : rawSegments) {
                boolean known = unique.stream().anyMatch(existing -> existing.label().equals(segment.label()));
                if (!known) {
                    unique.add(segment);
                }
            }

            int currentX = x;
            int currentY = y;
            g.setFont(g.getFont().deriveFont(11f));
            for (Segment segment : unique) {
                if (currentX > getWidth() - 180) {
                    currentX = x;
                    currentY += 20;
                }
                g.setColor(segment.color());
                g.fillRect(currentX, currentY - 10, 12, 12);
                g.setColor(new Color(40, 40, 40));
                g.drawString(segment.label(), currentX + 17, currentY);
                currentX += Math.max(110, g.getFontMetrics().stringWidth(segment.label()) + 32);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProbabilityCalculatorGUI().setVisible(true));
    }
}
