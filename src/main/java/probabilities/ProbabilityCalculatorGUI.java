package probabilities;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.EnumMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

public class ProbabilityCalculatorGUI extends JFrame {
    private static final Color WINDOW_BG = new Color(232, 232, 232);
    private static final Color PANEL_BG = new Color(244, 244, 244);
    private static final Color FIELD_BG = new Color(255, 252, 237);
    private static final Color INFO_FIELD_BG = new Color(236, 241, 247);
    private static final Color FIELD_BORDER = new Color(77, 119, 178);
    private static final Color INFO_FIELD_BORDER = new Color(150, 158, 169);
    private static final Color BORDER = new Color(150, 150, 150);
    private static final Color BUTTON_BLUE = new Color(214, 224, 239);
    private static final Color GRAPH_BLUE = new Color(75, 126, 190);
    private static final Color GRAPH_RED = new Color(195, 92, 82);
    private static final Color GRAPH_GREEN = new Color(90, 153, 106);
    private static final Color GRAPH_YELLOW = new Color(213, 169, 70);

    private final JComboBox<AnalysisType> analysisSelector = new JComboBox<>(AnalysisType.values());
    private final JComboBox<Language> languageSelector = new JComboBox<>(Language.values());
    private final JPanel inputHost = new ScrollableInputHost();
    private final JTextArea output = new JTextArea(14, 38);
    private final ChartPanel chartPanel = new ChartPanel();
    private final JLabel statusLabel = new JLabel();
    private JPanel headerPanel;
    private JPanel mainPanel;
    private Language language = Language.DE;
    private AnalysisType activeType;
    private final Map<String, JTextField> activeFields = new LinkedHashMap<>();
    private final Map<AnalysisType, Map<String, String>> drafts = new EnumMap<>(AnalysisType.class);
    private final List<HistoryStore.Entry> history = new ArrayList<>();
    private final HistoryStore historyStore = new HistoryStore(Path.of(System.getProperty("probabilitycalculator.dataDir",
            Path.of(System.getProperty("user.home"), ".probability-calculator").toString()), "history.xml"));
    private final ExecutorService historyWriter = Executors.newSingleThreadExecutor();
    private boolean rememberHistory = true;
    private boolean historyLoadFailed;
    private boolean historyWritable;
    private DefaultTableModel historyModel;
    private JTable historyTable;
    private JTextArea historyDetails;
    private JCheckBox historyRemember;
    private JTextField historySearch;
    private JComboBox<String> historyTypeFilter;
    private JCheckBox favoritesOnly;
    private TableRowSorter<DefaultTableModel> historySorter;
    private JLabel historyCount;
    private JCheckBox favoriteToggle;
    private JTabbedPane resultTabs;
    private SwingWorker<CalculationService.Result, Void> calculationWorker;
    private CalculationService.Result lastResult;
    private CalculationRequest lastRequest;
    private JButton calculateButton;
    private boolean rebuildingLanguage;

    private JTextField probAField;
    private JTextField probBField;
    private JTextField intersectionField;
    private JTextField likelihoodsField;
    private JTextField priorsField;
    private JTextField selectedIndexField;
    private JTextField trialsField;
    private JTextField successesField;
    private JTextField lowerField;
    private JTextField upperField;
    private JTextField probabilityField;
    private JTextField lambdaField;
    private JTextField meanField;
    private JTextField standardDeviationField;

    public ProbabilityCalculatorGUI() {
        super("Wahrscheinlichkeitsrechner");
        configureLookAndFeel();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 720));
        try { historyWritable = historyStore.acquireSessionLock(); }
        catch (IOException exception) { historyWritable = false; }
        try {
            HistoryStore.State state = historyStore.load();
            rememberHistory = state.remember();
            language = Language.valueOf(state.language());
            history.addAll(state.entries());
        } catch (IOException exception) {
            historyLoadFailed = true;
            rememberHistory = false;
        }
        setTitle(t("app.title"));
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent event) {
                cancelCalculation();
                historyWriter.execute(() -> {
                    try { historyStore.releaseSessionLock(); }
                    catch (IOException ignored) { /* The OS also releases the lock on exit. */ }
                });
                historyWriter.shutdown();
            }
        });
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(WINDOW_BG);

        headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);
        mainPanel = createMainContent();
        add(mainPanel, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        analysisSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AnalysisType type) {
                    setText(type.comboLabel(language));
                }
                return this;
            }
        });

        analysisSelector.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED && !rebuildingLanguage) {
                rebuildInputPanel();
            }
        });
        languageSelector.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                language = (Language) event.getItem();
                rebuildLanguage();
            }
        });
        rebuildInputPanel();

        pack();
        setLocationRelativeTo(null);
        if (historyLoadFailed) SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                t("history.loadError"), t("tab.history"), JOptionPane.WARNING_MESSAGE));
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

        JLabel title = new JLabel(t("app.title"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        header.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        header.add(new JLabel(t("header.family")), gbc);

        analysisSelector.setPreferredSize(new Dimension(270, 26));
        gbc.gridx = 1;
        header.add(analysisSelector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        header.add(new JLabel(t("header.inputMode")), gbc);

        JTextArea modeLabel = createHeaderInfoField(t("header.probabilityMode"));
        gbc.gridx = 1;
        header.add(modeLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        header.add(new JLabel(t("header.language")), gbc);

        languageSelector.setPreferredSize(new Dimension(110, 26));
        languageSelector.setSelectedItem(language);
        gbc.gridx = 3;
        header.add(languageSelector, gbc);

        gbc.gridx = 4;
        gbc.weightx = 1.0;
        header.add(new JLabel(), gbc);

        return header;
    }

    private JTextArea createHeaderInfoField(String text) {
        JTextArea field = new JTextArea(text);
        field.setEditable(false);
        field.setFocusable(false);
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setOpaque(true);
        field.setBackground(INFO_FIELD_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INFO_FIELD_BORDER),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        field.setFont(UIManager.getFont("Label.font"));
        field.setToolTipText(text);
        field.setSize(new Dimension(235, Short.MAX_VALUE));
        int height = Math.max(26, field.getPreferredSize().height);
        field.setPreferredSize(new Dimension(235, height));
        field.setMinimumSize(new Dimension(210, 26));
        return field;
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
        inputHost.setPreferredSize(new Dimension(400, 550));
        JScrollPane inputs = new JScrollPane(inputHost);
        inputs.setBorder(BorderFactory.createEmptyBorder());
        inputs.setPreferredSize(new Dimension(420, 550));
        inputs.setMinimumSize(new Dimension(410, 200));
        main.add(inputs, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        resultTabs = new JTabbedPane();
        resultTabs.setMinimumSize(new Dimension(520, 300));
        resultTabs.addTab(t("tab.results"), createOutputArea());
        resultTabs.addTab(t("tab.history"), createHistoryPanel());
        main.add(resultTabs, gbc);

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
        resultPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), t("panel.output")));
        resultPanel.add(new JScrollPane(output), BorderLayout.CENTER);
        JButton copy = new JButton(t("button.copy"));
        copy.addActionListener(event -> {
            if (!output.getText().isBlank()) {
                getToolkit().getSystemClipboard().setContents(new StringSelection(output.getText()), null);
                statusLabel.setText(t("status.copied"));
            }
        });
        JPanel resultActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 2));
        resultActions.add(copy);
        resultActions.add(actionButton(t("export.csv"), () -> {
            if (lastResult != null && lastRequest != null) {
                CalculationRequest request = lastRequest;
                List<CalculationService.Value> values = lastResult.values();
                exportFile("csv", path -> Files.writeString(path, ExportService.resultsCsv(request, values), StandardCharsets.UTF_8));
            }
        }));
        resultPanel.add(resultActions, BorderLayout.SOUTH);

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBackground(PANEL_BG);
        graphPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), t("panel.plot")));
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        JPanel graphActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 2));
        graphActions.add(infoButton(t("button.info"), t("info.plot")));
        graphActions.add(actionButton(t("export.png"), () -> {
            if (lastResult != null) {
                BufferedImage image = chartPanel.snapshot();
                exportFile("png", path -> {
                    if (!ImageIO.write(image, "png", path.toFile())) throw new IOException("PNG encoder unavailable");
                });
            }
        }));
        graphPanel.add(graphActions, BorderLayout.SOUTH);

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

    private void rebuildLanguage() {
        rebuildingLanguage = true;
        setTitle(t("app.title"));
        AnalysisType selectedType = (AnalysisType) analysisSelector.getSelectedItem();
        remove(headerPanel);
        headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);
        analysisSelector.setSelectedItem(selectedType);
        rebuildInputPanel();
        remove(mainPanel);
        mainPanel = createMainContent();
        add(mainPanel, BorderLayout.CENTER);
        if (lastResult != null) displayResult(lastResult);
        getRootPane().setDefaultButton(calculateButton);
        rebuildingLanguage = false;
        saveHistory();
        revalidate();
        repaint();
    }

    private void rebuildInputPanel() {
        cancelCalculation();
        if (activeType != null) drafts.put(activeType, captureInputs());
        activeType = (AnalysisType) analysisSelector.getSelectedItem();
        activeFields.clear();
        inputHost.removeAll();
        inputHost.add(createInputPanel((AnalysisType) analysisSelector.getSelectedItem()), BorderLayout.CENTER);
        registerFields();
        restoreInputs(drafts.get(activeType));
        activeFields.values().forEach(field -> field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                if (rebuildingLanguage) return;
                cancelCalculation();
                lastResult = null;
                output.setText("");
                chartPanel.clear(t("chart.empty"));
                statusLabel.setText(t("status.ready"));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { changed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { changed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { changed(); }
        }));
        inputHost.revalidate();
        inputHost.repaint();
        output.setText("");
        chartPanel.clear(t("chart.empty"));
        if (!rebuildingLanguage) lastResult = null;
        statusLabel.setText(t("status.ready"));
    }

    private JPanel createInputPanel(AnalysisType type) {
        FormPanel form = new FormPanel(t("panel.input"));
        form.addSection(t("section.analysis"));
        form.addReadOnly(t("field.selectedProcedure"), type.displayName(language));
        form.addReadOnly(t("field.tails"), type.tailDescription(language));
        form.addSeparator();
        form.addLegend(t("legend.input"), t("legend.info"));

        switch (type) {
            case COMPLEMENT -> createComplementInputs(form);
            case JOINT -> createJointInputs(form);
            case CONDITIONAL -> createConditionalInputs(form);
            case BAYES -> createBayesInputs(form);
            case BINOMIAL -> createBinomialInputs(form);
            case POISSON -> createPoissonInputs(form);
            case NORMAL -> createNormalInputs(form);
        }

        form.addBottomGlue();
        return form;
    }

    private void createComplementInputs(FormPanel form) {
        form.addSection(t("section.input"));
        probAField = form.addField("Pr(A)", "0.30");
        probBField = form.addField("Pr(B)", "0.40");
        form.addActionRow(
                actionButton(t("button.calculate"), this::calculateCurrent),
                actionButton(t("button.clear"), this::clearCurrent),
                infoButton(t("button.info"), t("info.complement"))
        );
    }

    private void createJointInputs(FormPanel form) {
        form.addSection(t("section.input"));
        probAField = form.addField("Pr(A)", "0.30");
        probBField = form.addField("Pr(B)", "0.40");
        intersectionField = form.addField("Pr(A and B)", "");
        form.addHint(t("hint.joint"));
        form.addActionRow(
                actionButton(t("button.calculate"), this::calculateCurrent),
                actionButton(t("button.clear"), this::clearCurrent),
                infoButton(t("button.info"), t("info.joint"))
        );
    }

    private void createConditionalInputs(FormPanel form) {
        form.addSection(t("section.input"));
        intersectionField = form.addField("Pr(A and B)", "0.12");
        probAField = form.addField("Pr(A)", "0.30");
        probBField = form.addField("Pr(B)", "0.40");
        form.addActionRow(
                actionButton(t("button.calculate"), this::calculateCurrent),
                actionButton(t("button.clear"), this::clearCurrent),
                infoButton(t("button.info"), t("info.conditional"))
        );
    }

    private void createBayesInputs(FormPanel form) {
        form.addSection(t("section.input"));
        likelihoodsField = form.addField("Pr(B|A_i)", "0.90; 0.20");
        priorsField = form.addField("Pr(A_i)", "0.30; 0.70");
        selectedIndexField = form.addField(t("field.requestedAi"), "1");
        form.addHint(t("hint.bayes"));
        form.addActionRow(
                actionButton(t("button.calculate"), this::calculateCurrent),
                actionButton(t("button.clear"), this::clearCurrent),
                infoButton(t("button.info"), t("info.bayes"))
        );
    }

    private void createBinomialInputs(FormPanel form) {
        form.addSection(t("section.input"));
        trialsField = form.addField(t("field.trials"), "10");
        successesField = form.addField(t("field.successes"), "3");
        lowerField = form.addField(t("field.lower"), "0");
        upperField = form.addField(t("field.upper"), "3");
        probabilityField = form.addField(t("field.successProbability"), "0.50");
        form.addHint(t("hint.binomial"));
        form.addActionRow(
                actionButton(t("button.calculate"), this::calculateCurrent),
                actionButton(t("button.clear"), this::clearCurrent),
                infoButton(t("button.info"), t("info.binomial") + "\n\n" + t("info.discreteExtras"))
        );
    }

    private void createPoissonInputs(FormPanel form) {
        form.addSection(t("section.input"));
        lambdaField = form.addField(t("field.lambda"), "3.00");
        successesField = form.addField(t("field.count"), "2");
        lowerField = form.addField(t("field.lower"), "0");
        upperField = form.addField(t("field.upper"), "4");
        form.addHint(t("hint.poisson"));
        form.addActionRow(
                actionButton(t("button.calculate"), this::calculateCurrent),
                actionButton(t("button.clear"), this::clearCurrent),
                infoButton(t("button.info"), t("info.poisson") + "\n\n" + t("info.discreteExtras"))
        );
    }

    private void createNormalInputs(FormPanel form) {
        form.addSection(t("section.input"));
        meanField = form.addField(t("field.mean"), "0.00");
        standardDeviationField = form.addField(t("field.stddev"), "1.00");
        lowerField = form.addField(t("field.lowerX"), "-1.00");
        upperField = form.addField(t("field.upperX"), "1.00");
        form.addHint(t("hint.normal"));
        form.addActionRow(
                actionButton(t("button.calculate"), this::calculateCurrent),
                actionButton(t("button.clear"), this::clearCurrent),
                infoButton(t("button.info"), t("info.normal"))
        );
    }

    private JButton actionButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setBackground(BUTTON_BLUE);
        if (label.equals(t("button.calculate"))) {
            calculateButton = button;
            getRootPane().setDefaultButton(button);
        }
        button.addActionListener(_event -> {
            try {
                action.run();
            } catch (IllegalArgumentException exception) {
                statusLabel.setText(t("status.checkInput"));
                String message = language == Language.EN && exception instanceof ProbabilityException validation
                        ? validation.englishMessage() : exception.getMessage();
                JOptionPane.showMessageDialog(this, message, t("dialog.checkInput"), JOptionPane.WARNING_MESSAGE);
            }
        });
        return button;
    }

    private JButton infoButton(String label, String message) {
        JButton button = new JButton(label);
        button.setBackground(new Color(232, 232, 232));
        button.addActionListener(_event -> {
            JTextArea help = new JTextArea(message.strip(), 20, 52);
            help.setEditable(false);
            help.setLineWrap(true);
            help.setWrapStyleWord(true);
            help.setFont(UIManager.getFont("Label.font"));
            help.setMargin(new Insets(8, 8, 8, 8));
            help.setCaretPosition(0);
            JOptionPane.showMessageDialog(this, new JScrollPane(help), t("button.info"), JOptionPane.PLAIN_MESSAGE);
        });
        return button;
    }

    private void registerFields() {
        switch (activeType) {
            case COMPLEMENT -> { activeFields.put("a", probAField); activeFields.put("b", probBField); }
            case JOINT, CONDITIONAL -> { activeFields.put("a", probAField); activeFields.put("b", probBField); activeFields.put("intersection", intersectionField); }
            case BAYES -> { activeFields.put("likelihoods", likelihoodsField); activeFields.put("priors", priorsField); activeFields.put("index", selectedIndexField); }
            case BINOMIAL -> { activeFields.put("n", trialsField); activeFields.put("k", successesField); activeFields.put("lower", lowerField); activeFields.put("upper", upperField); activeFields.put("p", probabilityField); }
            case POISSON -> { activeFields.put("lambda", lambdaField); activeFields.put("k", successesField); activeFields.put("lower", lowerField); activeFields.put("upper", upperField); }
            case NORMAL -> { activeFields.put("mean", meanField); activeFields.put("sd", standardDeviationField); activeFields.put("lower", lowerField); activeFields.put("upper", upperField); }
        }
        activeFields.forEach((key, field) -> field.getAccessibleContext().setAccessibleName(key));
    }

    private Map<String, String> captureInputs() {
        Map<String, String> inputs = new LinkedHashMap<>();
        activeFields.forEach((key, field) -> inputs.put(key, field.getText()));
        return inputs;
    }

    private void restoreInputs(Map<String, String> inputs) {
        if (inputs != null) activeFields.forEach((key, field) -> field.setText(inputs.getOrDefault(key, "")));
    }

    private void calculateCurrent() { calculateCurrent(true); }

    private void calculateCurrent(boolean record) {
        cancelCalculation();
        CalculationRequest request = new CalculationRequest(activeType.name(), captureInputs());
        output.setText("");
        lastResult = null;
        chartPanel.clear(t("chart.empty"));
        statusLabel.setText(t("status.calculating"));
        resultTabs.setSelectedIndex(0);
        calculationWorker = new SwingWorker<>() {
            @Override protected CalculationService.Result doInBackground() {
                return CalculationService.calculate(request);
            }
            @Override protected void done() {
                if (calculationWorker != this || isCancelled()) return;
                calculationWorker = null;
                try {
                    lastResult = get();
                    lastRequest = request;
                    displayResult(lastResult);
                    statusLabel.setText(t("status.success"));
                    if (record) {
                        history.add(0, new HistoryStore.Entry(Instant.now(), request, output.getText()));
                        HistoryStore.trim(history);
                        refreshHistory();
                        saveHistory();
                    }
                } catch (CancellationException ignored) {
                    statusLabel.setText(t("status.ready"));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText(t("status.checkInput"));
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    statusLabel.setText(t("status.checkInput"));
                    String message = language == Language.EN && cause instanceof ProbabilityException validation
                            ? validation.englishMessage() : cause.getMessage();
                    JOptionPane.showMessageDialog(ProbabilityCalculatorGUI.this,
                            cause instanceof IllegalArgumentException ? message : t("error.calculation"),
                            t("dialog.checkInput"), JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        calculationWorker.execute();
    }

    private void cancelCalculation() {
        if (calculationWorker != null) { calculationWorker.cancel(true); calculationWorker = null; }
    }

    private void clearCurrent() {
        cancelCalculation();
        activeFields.values().forEach(field -> field.setText(""));
        lastResult = null;
        output.setText("");
        chartPanel.clear(t("chart.empty"));
        statusLabel.setText(t("status.ready"));
    }

    private void displayResult(CalculationService.Result result) {
        String heading = t("heading." + result.heading());
        StringBuilder builder = new StringBuilder(heading).append("\n\n");
        Locale locale = language == Language.DE ? Locale.GERMANY : Locale.US;
        for (CalculationService.Value line : result.values()) {
            String numeric = String.format(locale, "%.8g", line.value());
            if (line.probability()) numeric += String.format(locale, "   %.4f%%", line.value() * 100);
            builder.append(label(line.label())).append(": ").append(numeric).append('\n');
        }
        output.setText(builder.toString());
        output.setCaretPosition(0);
        String chartTitle = t("chart." + activeType.name().toLowerCase(Locale.ROOT));
        chartPanel.locale = locale;
        if (result.normalPlot() != null) {
            chartPanel.setNormalCurve(result.normalPlot(), result.shares().stream().map(this::segment).toList(), chartTitle);
        } else if (!result.groups().isEmpty()) {
            chartPanel.setStackedBars(result.groups().stream().map(group -> new BarGroup(label(group.label()),
                    group.shares().stream().map(this::segment).toList())).toList(), chartTitle);
        } else if (!result.points().isEmpty()) {
            chartPanel.setDiscreteBars(result.points().stream().map(point -> new DataPoint(point.label(),
                    point.value(), point.selected() ? GRAPH_RED : GRAPH_BLUE)).toList(), chartTitle + " " + t("chart.bins"));
        } else {
            chartPanel.setSegments(result.shares().stream().map(this::segment).toList(), chartTitle);
        }
    }

    private Segment segment(CalculationService.Share share) {
        Color[] colors = {GRAPH_BLUE, GRAPH_RED, GRAPH_GREEN, GRAPH_YELLOW, new Color(165, 165, 165)};
        return new Segment(label(share.label()), share.value(), colors[share.color()]);
    }

    private String label(String key) {
        if (key.startsWith("segment.") || key.startsWith("result.")) return t(key);
        if (language == Language.EN) return key;
        return key.replace("not ", "nicht ").replace(" and ", " und ").replace(" or ", " oder ")
                .replace("neither", "weder A noch B").replace("lower", "unten").replace("upper", "oben");
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        historyModel = new DefaultTableModel(new String[]{t("history.favorite"), t("history.name"), t("history.time"), t("history.analysis")}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int column) { return column == 0 ? Boolean.class : String.class; }
        };
        historyTable = new JTable(historyModel);
        historySorter = new TableRowSorter<>(historyModel);
        historyTable.setRowSorter(historySorter);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(25);
        historyTable.setFont(historyTable.getFont().deriveFont(12f));
        historyTable.getColumnModel().getColumn(0).setMaxWidth(75);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        historySearch = new JTextField(12);
        historySearch.getAccessibleContext().setAccessibleName(t("history.search"));
        historyTypeFilter = new JComboBox<>();
        historyTypeFilter.addItem(t("history.all"));
        for (AnalysisType type : AnalysisType.values()) historyTypeFilter.addItem(type.comboLabel(language));
        favoritesOnly = new JCheckBox(t("history.favoritesOnly"));
        historyCount = new JLabel();
        JPanel filters = new JPanel(new GridBagLayout());
        GridBagConstraints filter = new GridBagConstraints();
        filter.insets = new Insets(0, 0, 4, 6);
        filters.add(new JLabel(t("history.search")), filter);
        filter.gridx = 1; filter.weightx = 1; filter.fill = GridBagConstraints.HORIZONTAL;
        filters.add(historySearch, filter);
        filter.gridx = 2; filter.weightx = 0;
        filters.add(infoButton(t("button.info"), t("info.history")), filter);
        filter.gridx = 0; filter.gridy = 1; filter.gridwidth = 2;
        filters.add(historyTypeFilter, filter);
        filter.gridx = 2; filter.gridwidth = 1;
        filters.add(favoritesOnly, filter);
        filter.gridx = 0; filter.gridy = 2; filter.gridwidth = 3;
        filters.add(historyCount, filter);
        panel.add(filters, BorderLayout.NORTH);
        historySearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { filterHistory(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { filterHistory(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { filterHistory(); }
        });
        historyTypeFilter.addActionListener(event -> filterHistory());
        favoritesOnly.addActionListener(event -> filterHistory());
        historyDetails = new JTextArea(9, 30);
        historyDetails.setEditable(false);
        historyDetails.setLineWrap(true);
        historyDetails.setWrapStyleWord(true);
        historyDetails.setMargin(new Insets(8, 8, 8, 8));
        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(historyTable), new JScrollPane(historyDetails));
        split.setResizeWeight(0.55);
        split.setBorder(BorderFactory.createEmptyBorder());
        panel.add(split, BorderLayout.CENTER);
        historyTable.getSelectionModel().addListSelectionListener(event -> {
            int row = selectedHistoryIndex();
            if (row >= 0) {
                HistoryStore.Entry entry = history.get(row);
                historyDetails.setText((entry.name().isBlank() ? "" : entry.name() + "\n\n")
                        + t("history.inputs") + "\n" + inputSummary(entry.request(), "\n")
                        + "\n\n" + entry.result());
                historyDetails.setCaretPosition(0);
                favoriteToggle.setSelected(entry.favorite());
            } else {
                historyDetails.setText(history.isEmpty() ? t("history.empty") : "");
                favoriteToggle.setSelected(false);
            }
            favoriteToggle.setEnabled(row >= 0);
        });
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        JButton restore = new JButton(t("history.restore"));
        restore.addActionListener(event -> {
            int row = selectedHistoryIndex();
            if (row < 0) return;
            CalculationRequest request = history.get(row).request();
            AnalysisType type = AnalysisType.valueOf(request.analysis());
            analysisSelector.setSelectedItem(type);
            restoreInputs(request.inputs());
            calculateCurrent(false);
        });
        JButton delete = new JButton(t("history.delete"));
        delete.addActionListener(event -> {
            int row = selectedHistoryIndex();
            if (row >= 0) { history.remove(row); refreshHistory(); saveHistory(); }
        });
        JButton clear = new JButton(t("history.clear"));
        clear.addActionListener(event -> {
            if (JOptionPane.showConfirmDialog(this, t("history.confirm"), t("tab.history"),
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                history.clear();
                historyLoadFailed = false;
                historyRemember.setEnabled(historyWritable);
                refreshHistory();
                saveHistory();
            }
        });
        actions.add(restore); actions.add(delete); actions.add(clear);
        JPanel management = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        favoriteToggle = new JCheckBox(t("history.favorite"));
        favoriteToggle.setToolTipText(t("history.favoriteTip"));
        favoriteToggle.addActionListener(event -> {
            int row = selectedHistoryIndex();
            if (row < 0) return;
            if (favoriteToggle.isSelected() && history.stream().filter(HistoryStore.Entry::favorite).count() >= HistoryStore.FAVORITE_LIMIT) {
                favoriteToggle.setSelected(false);
                JOptionPane.showMessageDialog(this, t("history.favoriteLimit"));
                return;
            }
            history.set(row, history.get(row).withFavorite(favoriteToggle.isSelected()));
            HistoryStore.trim(history);
            refreshHistory(); saveHistory();
        });
        JButton rename = actionButton(t("history.rename"), () -> {
            int row = selectedHistoryIndex();
            if (row < 0) return;
            String name = JOptionPane.showInputDialog(this, t("history.namePrompt"), history.get(row).name());
            if (name == null) return;
            if (name.length() > 80) { JOptionPane.showMessageDialog(this, t("history.nameLimit")); return; }
            history.set(row, history.get(row).withName(name));
            refreshHistory(); saveHistory();
        });
        management.add(favoriteToggle); management.add(rename);
        management.add(actionButton(t("export.csv"), () -> {
            List<HistoryStore.Entry> visible = new ArrayList<>();
            for (int row = 0; row < historyTable.getRowCount(); row++) {
                visible.add(history.get(historyTable.convertRowIndexToModel(row)));
            }
            if (!visible.isEmpty()) {
                exportFile("csv", path -> Files.writeString(path, ExportService.historyCsv(visible), StandardCharsets.UTF_8));
            }
        }));
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints control = new GridBagConstraints();
        control.gridx = 0; control.weightx = 1; control.fill = GridBagConstraints.HORIZONTAL;
        control.insets = new Insets(0, 0, 6, 0);
        JCheckBox remember = new JCheckBox(t("history.remember"), rememberHistory);
        historyRemember = remember;
        remember.setEnabled(!historyLoadFailed && historyWritable);
        remember.setToolTipText(t("history.privacy"));
        remember.addActionListener(event -> { rememberHistory = remember.isSelected(); saveHistory(); });
        controls.add(management, control);
        control.gridy = 1; controls.add(actions, control);
        control.gridy = 2; controls.add(remember, control);
        if (!historyWritable) {
            JTextArea notice = new JTextArea(t("history.readOnly"));
            notice.setEditable(false); notice.setLineWrap(true); notice.setWrapStyleWord(true);
            notice.setRows(3); notice.setBackground(controls.getBackground());
            control.gridy = 3; controls.add(notice, control);
        }
        panel.add(controls, BorderLayout.SOUTH);
        refreshHistory();
        if (historyTable.getRowCount() > 0) historyTable.setRowSelectionInterval(0, 0);
        return panel;
    }

    private String inputSummary(CalculationRequest request, String separator) {
        return request.inputs().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(java.util.stream.Collectors.joining(separator));
    }

    private void refreshHistory() {
        if (historyModel == null) return;
        int selected = selectedHistoryIndex();
        String selectedId = selected < 0 ? null : history.get(selected).id();
        historyModel.setRowCount(0);
        DateTimeFormatter time = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
        for (HistoryStore.Entry entry : history) {
            historyModel.addRow(new Object[]{entry.favorite(), entry.name(), time.format(entry.time()),
                    AnalysisType.valueOf(entry.request().analysis()).comboLabel(language)});
        }
        filterHistory();
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).id().equals(selectedId)) {
                int view = historyTable.convertRowIndexToView(i);
                if (view >= 0) historyTable.setRowSelectionInterval(view, view);
            }
        }
        if (historyTable.getSelectedRow() < 0) historyDetails.setText(history.isEmpty() ? t("history.empty") : "");
    }

    private int selectedHistoryIndex() {
        if (historyTable == null || historyTable.getSelectedRow() < 0) return -1;
        int row = historyTable.convertRowIndexToModel(historyTable.getSelectedRow());
        return row < history.size() ? row : -1;
    }

    private void filterHistory() {
        String query = historySearch.getText().strip().toLowerCase(Locale.ROOT);
        int type = historyTypeFilter.getSelectedIndex();
        historySorter.setRowFilter(new RowFilter<>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> row) {
                HistoryStore.Entry entry = history.get(row.getIdentifier());
                return (!favoritesOnly.isSelected() || entry.favorite())
                        && (type <= 0 || entry.request().analysis().equals(AnalysisType.values()[type - 1].name()))
                        && (entry.name() + " " + entry.request().analysis() + " " + inputSummary(entry.request(), " ")
                        + " " + entry.result() + " " + row.getStringValue(2) + " " + row.getStringValue(3))
                        .toLowerCase(Locale.ROOT).contains(query);
            }
        });
        historyCount.setText(historyTable.getRowCount() + " / " + history.size() + " " + t("history.entries"));
    }

    @FunctionalInterface
    private interface FileExport { void write(Path path) throws IOException; }

    private void exportFile(String extension, FileExport export) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(extension.toUpperCase(Locale.ROOT), extension));
        chooser.setSelectedFile(new java.io.File("probability-calculator." + extension));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path chosen = chooser.getSelectedFile().toPath();
        if (!chosen.toString().toLowerCase(Locale.ROOT).endsWith("." + extension)) chosen = Path.of(chosen + "." + extension);
        Path target = chosen;
        if (Files.exists(target) && JOptionPane.showConfirmDialog(this, t("export.overwrite"), t("export.title"),
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        historyWriter.execute(() -> {
            try {
                export.write(target);
                SwingUtilities.invokeLater(() -> statusLabel.setText(t("export.success")));
            } catch (IOException exception) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, t("export.error"),
                        t("export.title"), JOptionPane.ERROR_MESSAGE));
            }
        });
    }

    private void saveHistory() {
        if (!historyWritable || historyLoadFailed || historyWriter.isShutdown()) return;
        HistoryStore.State state = new HistoryStore.State(rememberHistory, language.name(), history);
        historyWriter.execute(() -> {
            try { historyStore.save(state); }
            catch (IOException exception) {
                SwingUtilities.invokeLater(() -> statusLabel.setText(t("history.saveError")));
            }
        });
    }

    private String t(String key) {
        boolean de = language == Language.DE;
        return switch (key) {
            case "app.title" -> de ? "Wahrscheinlichkeitsrechner" : "Probability Calculator";
            case "header.family" -> de ? "Testfamilie" : "Test family";
            case "header.inputMode" -> de ? "Zahlenformat" : "Number format";
            case "header.probabilityMode" -> de ? "Dezimalpunkt oder -komma" : "Decimal point or comma";
            case "header.language" -> de ? "Sprache" : "Language";
            case "panel.input" -> de ? "Eingabeparameter" : "Input parameters";
            case "panel.output" -> de ? "Ausgabeparameter" : "Output parameters";
            case "panel.plot" -> de ? "Wahrscheinlichkeitsdiagramm" : "Probability plot";
            case "section.analysis" -> de ? "Analyse" : "Analysis";
            case "section.input" -> de ? "Eingabeparameter" : "Input parameters";
            case "legend.input" -> de ? "Gelb/blau: Eingabefeld" : "Yellow/blue: editable input";
            case "legend.info" -> de ? "Grau/blau: Info-Feld" : "Grey/blue: information field";
            case "field.selectedProcedure" -> de ? "Gewähltes Verfahren" : "Selected procedure";
            case "field.tails" -> de ? "Bereich" : "Tail(s)";
            case "field.requestedAi" -> de ? "Gesuchtes A_i" : "Requested A_i";
            case "field.trials" -> de ? "Anzahl Versuche n" : "Number of trials n";
            case "field.successes" -> de ? "Genaue Treffer k" : "Exact successes k";
            case "field.lower" -> de ? "Untere Grenze" : "Lower bound";
            case "field.upper" -> de ? "Obere Grenze" : "Upper bound";
            case "field.successProbability" -> de ? "Trefferwahrscheinlichkeit p" : "Success probability p";
            case "field.lambda" -> de ? "Rate lambda" : "Rate lambda";
            case "field.count" -> de ? "Genaue Anzahl k" : "Exact count k";
            case "field.mean" -> de ? "Mittelwert mu" : "Mean mu";
            case "field.stddev" -> de ? "Standardabweichung sigma" : "Std. deviation sigma";
            case "field.lowerX" -> de ? "Untere x-Grenze" : "Lower x";
            case "field.upperX" -> de ? "Obere x-Grenze" : "Upper x";
            case "button.calculate" -> de ? "Berechnen" : "Calculate";
            case "button.clear" -> de ? "Leeren" : "Clear";
            case "button.info" -> "Info";
            case "button.copy" -> de ? "Ergebnis kopieren" : "Copy result";
            case "status.copied" -> de ? "Ergebnis kopiert" : "Result copied";
            case "status.calculating" -> de ? "Berechnung laeuft..." : "Calculating...";
            case "error.calculation" -> de ? "Die Berechnung konnte nicht abgeschlossen werden. Bitte Eingaben pruefen." : "The calculation could not be completed. Please check the inputs.";
            case "tab.results" -> de ? "Ergebnisse" : "Results";
            case "tab.history" -> de ? "Verlauf" : "History";
            case "history.time" -> de ? "Zeitpunkt" : "Time";
            case "history.analysis" -> de ? "Berechnung" : "Calculation";
            case "history.inputs" -> de ? "Eingaben" : "Inputs";
            case "history.restore" -> de ? "Erneut laden" : "Restore";
            case "history.delete" -> de ? "Eintrag loeschen" : "Delete entry";
            case "history.clear" -> de ? "Verlauf leeren" : "Clear history";
            case "history.confirm" -> de ? "Alle Eintraege im Verlauf loeschen?" : "Delete all history entries?";
            case "history.remember" -> de ? "Verlauf lokal speichern" : "Save history locally";
            case "history.search" -> de ? "Suche" : "Search";
            case "history.all" -> de ? "Alle Berechnungsarten" : "All calculation types";
            case "history.favorite" -> de ? "Favorit" : "Favorite";
            case "history.favoritesOnly" -> de ? "Nur Favoriten" : "Favorites only";
            case "history.favoriteTip" -> de ? "Favoriten bleiben beim automatischen Aufraeumen erhalten." : "Favorites are retained during automatic history cleanup.";
            case "history.favoriteLimit" -> de ? "Maximal 100 Favoriten. Entferne zuerst eine andere Markierung." : "Maximum 100 favorites. Unmark another entry first.";
            case "history.name" -> de ? "Name" : "Name";
            case "history.rename" -> de ? "Benennen" : "Rename";
            case "history.namePrompt" -> de ? "Name fuer diesen Eintrag (max. 80 Zeichen):" : "Name for this entry (max. 80 characters):";
            case "history.nameLimit" -> de ? "Der Name darf maximal 80 Zeichen enthalten." : "The name must not exceed 80 characters.";
            case "history.entries" -> de ? "Eintraege" : "entries";
            case "history.readOnly" -> de ? "Lokales Speichern ist in diesem Fenster gesperrt. Ein anderes Fenster nutzt den Verlauf oder die Datei ist nicht beschreibbar. Aenderungen gelten nur fuer diese Sitzung." : "Local saving is unavailable in this window. Another window owns the history or the file is not writable. Changes apply to this session only.";
            case "export.csv" -> "CSV";
            case "export.png" -> "PNG";
            case "export.title" -> de ? "Export" : "Export";
            case "export.overwrite" -> de ? "Vorhandene Datei ersetzen?" : "Replace the existing file?";
            case "export.success" -> de ? "Datei exportiert." : "File exported.";
            case "export.error" -> de ? "Die Datei konnte nicht geschrieben werden." : "The file could not be written.";
            case "info.plot" -> de ? "Bewege den Mauszeiger ueber einen Balken oder die Normalverteilung, um Werte zu sehen. Die Normalverteilung zeigt die Dichtekurve von Mittelwert minus 4 bis plus 4 Standardabweichungen. Die farbigen Flaechen unterscheiden Werte unterhalb, innerhalb und oberhalb des eingegebenen Intervalls. Wahrscheinlichkeiten werden exakt mit der Verteilungsfunktion berechnet, auch ausserhalb des sichtbaren Bereichs. f(x) ist eine Dichte, keine Wahrscheinlichkeit. PNG exportiert das aktuelle Diagramm mit doppelter Aufloesung. CSV bei den Ergebnissen exportiert Originaleingaben, Zahlenwerte und Prozentwerte; bei Dichten, Mittelwerten und Varianzen bleibt die Prozentspalte leer." : "Hover over a bar or the normal distribution to inspect values. The normal plot shows the density curve from the mean minus 4 to plus 4 standard deviations. Colored areas distinguish values below, inside and above the input interval. Probabilities use the distribution function, including tails outside the visible range. f(x) is a density, not a probability. PNG exports the current plot at double resolution. Result CSV includes original inputs, numerical values and percentages; densities, means and variances have no percentage.";
            case "history.privacy" -> de ? "Nur auf diesem Rechner. Deaktivieren entfernt gespeicherte Berechnungen; die aktuelle Sitzung bleibt sichtbar." : "Only on this computer. Disabling removes saved calculations; the current session remains visible.";
            case "history.empty" -> de ? "Noch keine gespeicherten Berechnungen." : "No calculations saved yet.";
            case "history.saveError" -> de ? "Verlauf konnte nicht gespeichert werden; Eintraege bleiben in dieser Sitzung erhalten." : "History could not be saved; entries remain available in this session.";
            case "history.loadError" -> de ? "Der gespeicherte Verlauf ist nicht lesbar. Die Datei bleibt erhalten und wird nicht ueberschrieben. Mit 'Verlauf leeren' kann sie zurueckgesetzt werden." : "The saved history could not be read. The file is preserved and will not be overwritten. Use 'Clear history' to reset it.";
            case "chart.bins" -> de ? "(Bereich 0,01-99,99%; ggf. gruppiert)" : "(0.01-99.99% range; grouped if needed)";
            case "segment.intersection" -> de ? "A und B" : "A and B";
            case "segment.neither" -> de ? "Weder A noch B" : "Neither A nor B";
            case "segment.givenA" -> de ? "Gegeben A" : "Given A";
            case "segment.givenB" -> de ? "Gegeben B" : "Given B";
            case "segment.other" -> de ? "Weitere A_i" : "Other A_i";
            case "info.discreteExtras" -> de ? "Zusaetzliche Ergebnisse: Pr(X > k) ist die Wahrscheinlichkeit fuer mehr als k Ereignisse. SD(X) = sqrt(Var(X)) beschreibt die Streuung in der Einheit der Zaehlwerte. Erwartungswert, Varianz, Standardabweichung und Dichte sind keine Prozentwerte. Das Diagramm zeigt den zentralen Bereich zwischen den Quantilen 0,01% und 99,99%; bei grossen Bereichen werden benachbarte Zaehlwerte zusammengefasst. Rote Balken enthalten den gewaehlten Wert k. Die numerischen Ergebnisse verwenden immer die eingegebenen Grenzen, unabhaengig vom Diagrammausschnitt. Fuer Poisson gilt 0 < lambda <= 1e9." : "Additional results: Pr(X > k) is the probability of more than k events. SD(X) = sqrt(Var(X)) describes spread in count units. Mean, variance, standard deviation and density are not percentages. The plot shows the central range between the 0.01% and 99.99% quantiles; large ranges aggregate adjacent counts. Red bars contain the selected value k. Numerical results always use the entered bounds regardless of the plotted range. For Poisson, 0 < lambda <= 1e9.";
            case "info.history" -> de ? "Der Verlauf behaelt 100 aktuelle Berechnungen und zusaetzlich bis zu 100 Favoriten. Favoriten werden nicht automatisch entfernt und koennen benannt werden. Die Suche umfasst Namen, Eingaben, Berechnungsarten und Ergebnisse. Filter und Spaltensortierung bestimmen, welche Eintraege CSV exportiert. Der Export enthaelt Originaleingaben und den gespeicherten Ergebnistext in UTF-8. 'Erneut laden' berechnet einen Eintrag in der aktuellen Sprache, ohne ein Duplikat zu erstellen. Lokale Speicherung erfolgt ausschliesslich in ~/.probability-calculator/history.xml. Deaktivieren entfernt gespeicherte Berechnungen; die aktuelle Sitzung bleibt erhalten. Bei mehreren Fenstern darf nur das zuerst gestartete Fenster speichern, damit nichts ueberschrieben wird. Weitere Fenster arbeiten nur in ihrer Sitzung. Es werden keine Daten uebertragen." : "History retains 100 recent calculations plus up to 100 favorites. Favorites are not removed automatically and can be named. Search covers names, inputs, calculation types and results. Filters and column sorting determine which entries CSV exports. Export includes original inputs and saved result text in UTF-8. Restore recalculates in the current language without creating a duplicate. Local saving uses only ~/.probability-calculator/history.xml. Disabling removes saved calculations while retaining the current session. With multiple windows, only the first may save to prevent overwrites. Other windows work in their session only. No data is transmitted.";
            case "status.ready" -> de ? "Bereit" : "Ready";
            case "status.success" -> de ? "Berechnung erfolgreich" : "Calculation successful";
            case "status.checkInput" -> de ? "Eingabe prüfen" : "Check input";
            case "dialog.checkInput" -> de ? "Eingabe prüfen" : "Check input";
            case "chart.empty" -> de ? "Diagramme erscheinen nach der Berechnung." : "Charts appear after calculation.";
            case "hint.joint" -> de ? "Pr(A and B) leer lassen, um Unabhängigkeit anzunehmen." : "Leave Pr(A and B) empty to assume independence.";
            case "hint.bayes" -> de ? "Listenwerte mit Semikolon oder Leerzeichen trennen. Pr(A_i) muss zusammen 1 ergeben." : "Separate list values with semicolon or space. Pr(A_i) must sum to 1.";
            case "hint.binomial" -> de ? "Berechnet Pr(X = k), Pr(X <= k) und Pr(lower <= X <= upper)." : "Calculates Pr(X = k), Pr(X <= k), and Pr(lower <= X <= upper).";
            case "hint.poisson" -> de ? "Unabhaengige Zaehlwerte; 0 < lambda <= 1e9. Grenzen und k sind nichtnegative ganze Zahlen." : "Independent counts; 0 < lambda <= 1e9. Bounds and k are non-negative integers.";
            case "hint.normal" -> de ? "Berechnet Pr(X <= lower), Pr(lower <= X <= upper) und Pr(X > upper)." : "Calculates Pr(X <= lower), Pr(lower <= X <= upper), and Pr(X > upper).";
            case "heading.complement" -> de ? "Komplementwahrscheinlichkeiten" : "Complement probabilities";
            case "heading.joint" -> de ? "Schnitt und Vereinigung" : "Intersection and union";
            case "heading.jointIndependent" -> de ? "Schnitt und Vereinigung (Unabhängigkeit angenommen)" : "Intersection and union (independence assumed)";
            case "heading.conditional" -> de ? "Bedingte Wahrscheinlichkeiten" : "Conditional probabilities";
            case "heading.bayes" -> de ? "Bayes und totale Wahrscheinlichkeit" : "Bayes and total probability";
            case "heading.binomial" -> de ? "Binomialverteilung" : "Binomial distribution";
            case "heading.poisson" -> de ? "Poissonverteilung" : "Poisson distribution";
            case "heading.normal" -> de ? "Normalverteilung" : "Normal distribution";
            case "chart.complement" -> de ? "Komplement-Zerlegung" : "Complement decomposition";
            case "chart.joint" -> de ? "Vierfelder-Zerlegung des Ergebnisraums" : "Four-part decomposition of the sample space";
            case "chart.conditional" -> de ? "Bedingte Anteile im jeweiligen Grundraum" : "Conditional proportions in each reference space";
            case "chart.bayes" -> de ? "Beiträge zu Pr(B) = Summe Pr(B|A_i) * Pr(A_i)" : "Contributions to Pr(B) = sum Pr(B|A_i) * Pr(A_i)";
            case "chart.binomial" -> de ? "Binomialverteilung Pr(X = k)" : "Binomial distribution Pr(X = k)";
            case "chart.poisson" -> de ? "Poissonverteilung Pr(X = k)" : "Poisson distribution Pr(X = k)";
            case "chart.normal" -> de ? "Normalverteilung: Dichte und Flaechen" : "Normal distribution: density and areas";
            case "result.aWithoutB" -> de ? "Pr(A ohne B)" : "Pr(A without B)";
            case "result.bWithoutA" -> de ? "Pr(B ohne A)" : "Pr(B without A)";
            case "result.contribution" -> de ? "Beitrag" : "Contribution";
            case "segment.aOnly" -> de ? "Nur A" : "A only";
            case "segment.bOnly" -> de ? "Nur B" : "B only";
            case "segment.leftTail" -> de ? "Linke Fläche" : "Left tail";
            case "segment.between" -> de ? "Zwischenbereich" : "Between";
            case "segment.rightTail" -> de ? "Rechte Fläche" : "Right tail";
            case "info.complement" -> de ? """
                    Zweck:
                    Nutze diese Ansicht, wenn du zu einer bekannten Wahrscheinlichkeit das Gegenereignis berechnen willst.

                    Eingaben:
                    Pr(A): Wahrscheinlichkeit, dass Ereignis A eintritt. Wert zwischen 0 und 1.
                    Pr(B): Wahrscheinlichkeit, dass Ereignis B eintritt. Wert zwischen 0 und 1.

                    Ausgaben:
                    Pr(not A): Wahrscheinlichkeit, dass A nicht eintritt.
                    Pr(not B): Wahrscheinlichkeit, dass B nicht eintritt.

                    Interpretation:
                    Wenn Pr(A) = 0.30 ist, dann ist Pr(not A) = 0.70. Das bedeutet: In 70 Prozent der Fälle tritt A nicht ein.

                    Diagramm:
                    Die Balken zeigen Ereignis und Gegenereignis zusammen als vollständigen Ergebnisraum von 100 Prozent.
                    """ : """
                    Purpose:
                    Use this view when you know a probability and want the probability of the complementary event.

                    Inputs:
                    Pr(A): Probability that event A occurs. Value between 0 and 1.
                    Pr(B): Probability that event B occurs. Value between 0 and 1.

                    Outputs:
                    Pr(not A): Probability that A does not occur.
                    Pr(not B): Probability that B does not occur.

                    Interpretation:
                    If Pr(A) = 0.30, then Pr(not A) = 0.70. This means A does not occur in 70 percent of cases.

                    Chart:
                    The bars show each event and its complement as the full sample space of 100 percent.
                    """;
            case "info.joint" -> de ? """
                    Zweck:
                    Nutze diese Ansicht, wenn du wissen willst, wie zwei Ereignisse A und B zusammen den Ergebnisraum aufteilen.

                    Eingaben:
                    Pr(A): Wahrscheinlichkeit von A.
                    Pr(B): Wahrscheinlichkeit von B.
                    Pr(A and B): Wahrscheinlichkeit, dass A und B gleichzeitig eintreten. Optional.

                    Wichtig:
                    Wenn Pr(A and B) leer bleibt, nimmt die App Unabhängigkeit an und berechnet Pr(A and B) = Pr(A) * Pr(B).
                    Wenn du einen eigenen Schnittwert eingibst, muss er logisch zu Pr(A) und Pr(B) passen.

                    Ausgaben:
                    Pr(A and B): gemeinsamer Anteil.
                    Pr(A or B): A oder B oder beide.
                    Pr(A without B): A tritt ein, B nicht.
                    Pr(B without A): B tritt ein, A nicht.
                    Pr(neither): weder A noch B.

                    Anwendung:
                    Gut für Vierfeldertafeln, überlappende Ereignisse und Plausibilitätschecks von Wahrscheinlichkeitsangaben.
                    """ : """
                    Purpose:
                    Use this view to see how two events A and B partition the sample space.

                    Inputs:
                    Pr(A): Probability of A.
                    Pr(B): Probability of B.
                    Pr(A and B): Probability that A and B occur together. Optional.

                    Important:
                    If Pr(A and B) is empty, the app assumes independence and calculates Pr(A and B) = Pr(A) * Pr(B).
                    If you enter your own intersection, it must be logically compatible with Pr(A) and Pr(B).

                    Outputs:
                    Pr(A and B): shared part.
                    Pr(A or B): A or B or both.
                    Pr(A without B): A occurs, B does not.
                    Pr(B without A): B occurs, A does not.
                    Pr(neither): neither A nor B occurs.

                    Use case:
                    Useful for contingency tables, overlapping events, and plausibility checks of probability values.
                    """;
            case "info.conditional" -> de ? """
                    Zweck:
                    Nutze diese Ansicht, wenn du Wahrscheinlichkeiten unter einer Bedingung berechnen willst.

                    Eingaben:
                    Pr(A and B): Wahrscheinlichkeit, dass A und B gemeinsam eintreten.
                    Pr(A): Grundwahrscheinlichkeit von A.
                    Pr(B): Grundwahrscheinlichkeit von B.

                    Ausgaben:
                    Pr(A|B): Wahrscheinlichkeit von A, wenn B bereits gilt.
                    Pr(B|A): Wahrscheinlichkeit von B, wenn A bereits gilt.

                    Interpretation:
                    Pr(A|B) betrachtet nicht mehr den gesamten Ergebnisraum, sondern nur noch die Fälle, in denen B eingetreten ist.

                    Anwendung:
                    Hilfreich bei Diagnostik, Filterbedingungen, Teilgruppen und Vierfeldertafeln.
                    """ : """
                    Purpose:
                    Use this view to calculate probabilities under a condition.

                    Inputs:
                    Pr(A and B): Probability that A and B occur together.
                    Pr(A): Base probability of A.
                    Pr(B): Base probability of B.

                    Outputs:
                    Pr(A|B): Probability of A given that B is true.
                    Pr(B|A): Probability of B given that A is true.

                    Interpretation:
                    Pr(A|B) no longer uses the whole sample space. It only looks at cases where B occurred.

                    Use case:
                    Helpful for diagnostics, filters, subgroups, and contingency tables.
                    """;
            case "info.bayes" -> de ? """
                    Zweck:
                    Nutze diese Ansicht, wenn mehrere mögliche Ursachen A_i zu einem beobachteten Ereignis B führen können.

                    Eingaben:
                    Pr(B|A_i): Liste der Wahrscheinlichkeiten für B unter jeder Ursache A_i.
                    Pr(A_i): Liste der Vorwahrscheinlichkeiten der Ursachen. Diese Werte müssen zusammen 1 ergeben.
                    Gesuchtes A_i: Index der Ursache, für die Pr(A_i|B) berechnet werden soll.

                    Ausgaben:
                    Pr(B): Gesamtwahrscheinlichkeit von B über alle Ursachen hinweg.
                    Pr(A_i|B): aktualisierte Wahrscheinlichkeit der gewählten Ursache nach Beobachtung von B.
                    Contribution A_i: Beitrag jeder Ursache zu Pr(B).

                    Interpretation:
                    Bayes aktualisiert Vorwissen. Aus Pr(A_i) wird nach Beobachtung von B die Posterior-Wahrscheinlichkeit Pr(A_i|B).

                    Anwendung:
                    Diagnostische Tests, Fehlerursachen, Klassifikation und Entscheidungsunterstützung.
                    """ : """
                    Purpose:
                    Use this view when several possible causes A_i can lead to an observed event B.

                    Inputs:
                    Pr(B|A_i): List of probabilities for B under each cause A_i.
                    Pr(A_i): List of prior probabilities of the causes. These values must sum to 1.
                    Requested A_i: Index of the cause for which Pr(A_i|B) should be calculated.

                    Outputs:
                    Pr(B): Total probability of B across all causes.
                    Pr(A_i|B): Updated probability of the selected cause after observing B.
                    Contribution A_i: Contribution of each cause to Pr(B).

                    Interpretation:
                    Bayes updates prior knowledge. Pr(A_i) becomes the posterior probability Pr(A_i|B) after observing B.

                    Use case:
                    Diagnostic tests, root-cause analysis, classification, and decision support.
                    """;
            case "info.binomial" -> de ? """
                    Zweck:
                    Nutze diese Ansicht für eine feste Anzahl unabhängiger Versuche mit genau zwei möglichen Ausgängen: Erfolg oder Misserfolg.

                    Eingaben:
                    n: Anzahl der Versuche, zum Beispiel 10 Würfe oder 100 getestete Personen.
                    k: genaue Trefferzahl, deren Wahrscheinlichkeit berechnet werden soll.
                    lower und upper: Grenzen für einen Trefferbereich.
                    p: Erfolgswahrscheinlichkeit pro Versuch, zwischen 0 und 1.

                    Ausgaben:
                    Pr(X = k): Wahrscheinlichkeit für genau k Treffer.
                    Pr(X <= k): Wahrscheinlichkeit für höchstens k Treffer.
                    Pr(lower <= X <= upper): Wahrscheinlichkeit für einen Trefferbereich.
                    E(X): erwartete Trefferzahl.
                    Var(X): Streuung der Trefferzahl.

                    Anwendung:
                    Qualitätskontrolle, Trefferquoten, Multiple-Choice-Aufgaben, Erfolgs-/Misserfolgsmodelle.
                    """ : """
                    Purpose:
                    Use this view for a fixed number of independent trials with exactly two outcomes: success or failure.

                    Inputs:
                    n: Number of trials, for example 10 throws or 100 tested people.
                    k: Exact number of successes whose probability should be calculated.
                    lower and upper: Bounds for a success interval.
                    p: Success probability per trial, between 0 and 1.

                    Outputs:
                    Pr(X = k): Probability of exactly k successes.
                    Pr(X <= k): Probability of at most k successes.
                    Pr(lower <= X <= upper): Probability of a success interval.
                    E(X): Expected number of successes.
                    Var(X): Variance of the number of successes.

                    Use case:
                    Quality control, hit rates, multiple-choice tasks, success/failure models.
                    """;
            case "info.poisson" -> de ? """
                    Zweck:
                    Nutze diese Ansicht für seltene oder zufällige Ereignisse pro Zeit-, Raum- oder Mengeneinheit.

                    Eingaben:
                    lambda: erwartete Ereignisanzahl im betrachteten Intervall. Muss größer als 0 sein.
                    k: genaue Anzahl an Ereignissen.
                    lower und upper: Intervallgrenzen für einen Ereignisbereich.

                    Ausgaben:
                    Pr(X = k): Wahrscheinlichkeit für genau k Ereignisse.
                    Pr(X <= k): Wahrscheinlichkeit für höchstens k Ereignisse.
                    Pr(lower <= X <= upper): Wahrscheinlichkeit für einen Ereignisbereich.
                    E(X) und Var(X): Bei Poisson beide gleich lambda.

                    Anwendung:
                    Anrufe pro Stunde, Fehler pro Seite, Ausfälle pro Zeitraum, Ereignisse pro Fläche.
                    """ : """
                    Purpose:
                    Use this view for rare or random events per time, space, or quantity interval.

                    Inputs:
                    lambda: Expected event count in the interval. Must be greater than 0.
                    k: Exact number of events.
                    lower and upper: Bounds for an event-count interval.

                    Outputs:
                    Pr(X = k): Probability of exactly k events.
                    Pr(X <= k): Probability of at most k events.
                    Pr(lower <= X <= upper): Probability of an event-count interval.
                    E(X) and Var(X): For Poisson both equal lambda.

                    Use case:
                    Calls per hour, defects per page, failures per period, events per area.
                    """;
            case "info.normal" -> de ? """
                    Zweck:
                    Nutze diese Ansicht für stetige Messwerte, die ungefähr normalverteilt sind.

                    Eingaben:
                    mu: Mittelwert, also Zentrum der Verteilung.
                    sigma: Standardabweichung. Sie muss größer als 0 sein und beschreibt die Streuung.
                    lower und upper: Grenzen des Bereichs, dessen Wahrscheinlichkeit berechnet werden soll.

                    Ausgaben:
                    Pr(X <= lower): linke Fläche bis zur unteren Grenze.
                    Pr(lower <= X <= upper): Fläche zwischen den Grenzen.
                    Pr(X > upper): rechte Fläche oberhalb der oberen Grenze.
                    f(mu): Dichte am Mittelwert. Das ist keine Wahrscheinlichkeit, sondern die Höhe der Dichtekurve.

                    Anwendung:
                    Messfehler, Körpergrößen, Prüfwerte, z-ähnliche Bereiche und Normalapproximationen.
                    """ : """
                    Purpose:
                    Use this view for continuous measurements that are approximately normally distributed.

                    Inputs:
                    mu: Mean, the center of the distribution.
                    sigma: Standard deviation. It must be greater than 0 and describes spread.
                    lower and upper: Bounds of the interval whose probability should be calculated.

                    Outputs:
                    Pr(X <= lower): Left area up to the lower bound.
                    Pr(lower <= X <= upper): Area between the bounds.
                    Pr(X > upper): Right area above the upper bound.
                    f(mu): Density at the mean. This is not a probability, but the height of the density curve.

                    Use case:
                    Measurement errors, heights, test values, z-like intervals, and normal approximations.
                    """;
            default -> key;
        };
    }

    private enum Language {
        DE("Deutsch"),
        EN("English");

        private final String label;

        Language(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum AnalysisType {
        COMPLEMENT("Exact: Complements", "Exakt: Komplemente", "event / complement", "Ereignis / Gegenereignis", "Complements", "Komplemente"),
        JOINT("Exact: Joint probability", "Exakt: Schnittwahrscheinlichkeit", "sample space", "Ergebnisraum", "Joint & union", "Schnitt & Oder"),
        CONDITIONAL("Exact: Conditional probability", "Exakt: Bedingte Wahrscheinlichkeit", "conditioning event", "Bedingungsereignis", "Conditional", "Bedingt"),
        BAYES("Bayes: Total probability", "Bayes: Totale Wahrscheinlichkeit", "posterior", "posterior", "Bayes", "Bayes"),
        BINOMIAL("Distribution: Binomial", "Verteilung: Binomial", "lower / interval", "untere Grenze / Intervall", "Binomial", "Binomial"),
        POISSON("Distribution: Poisson", "Verteilung: Poisson", "lower / interval", "untere Grenze / Intervall", "Poisson", "Poisson"),
        NORMAL("Distribution: Normal", "Verteilung: Normal", "tails / interval", "Ränder / Intervall", "Normal", "Normal");

        private final String displayNameEn;
        private final String displayNameDe;
        private final String tailDescriptionEn;
        private final String tailDescriptionDe;
        private final String comboLabelEn;
        private final String comboLabelDe;

        AnalysisType(String displayNameEn, String displayNameDe, String tailDescriptionEn, String tailDescriptionDe, String comboLabelEn, String comboLabelDe) {
            this.displayNameEn = displayNameEn;
            this.displayNameDe = displayNameDe;
            this.tailDescriptionEn = tailDescriptionEn;
            this.tailDescriptionDe = tailDescriptionDe;
            this.comboLabelEn = comboLabelEn;
            this.comboLabelDe = comboLabelDe;
        }

        private String displayName(Language language) {
            return language == Language.DE ? displayNameDe : displayNameEn;
        }

        private String tailDescription(Language language) {
            return language == Language.DE ? tailDescriptionDe : tailDescriptionEn;
        }

        private String comboLabel(Language language) {
            return language == Language.DE ? comboLabelDe : comboLabelEn;
        }

        @Override
        public String toString() {
            return comboLabelDe;
        }
    }

    private record Segment(String label, double value, Color color) {
    }

    private record BarGroup(String label, List<Segment> segments) {
    }

    private record DataPoint(String label, double value, Color color) {
    }

    private static final class ScrollableInputHost extends JPanel implements javax.swing.Scrollable {
        private ScrollableInputHost() { super(new BorderLayout()); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) { return 20; }
        @Override public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) { return visible.height - 20; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return getParent() != null && getParent().getHeight() >= getPreferredSize().height; }
    }

    private static final class FormPanel extends JPanel {
        private static final int LABEL_WIDTH = 155;
        private static final int VALUE_WIDTH = 190;
        private final GridBagConstraints constraints = new GridBagConstraints();
        private int row = 0;

        private FormPanel(String title) {
            super(new GridBagLayout());
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), title));
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
            JTextField field = new JTextField(value, 14);
            field.setBackground(FIELD_BG);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_BORDER),
                    BorderFactory.createEmptyBorder(3, 5, 3, 5)
            ));
            field.setToolTipText(label);
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.weightx = 0.0;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            add(createWrappedText(label, LABEL_WIDTH), constraints);
            constraints.gridx = 1;
            constraints.weightx = 1.0;
            constraints.anchor = GridBagConstraints.CENTER;
            add(field, constraints);
            row++;
            return field;
        }

        private void addReadOnly(String label, String value) {
            JTextArea valueLabel = createWrappedText(value, VALUE_WIDTH);
            valueLabel.setOpaque(true);
            valueLabel.setBackground(INFO_FIELD_BG);
            valueLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(INFO_FIELD_BORDER),
                    BorderFactory.createEmptyBorder(3, 5, 3, 5)
            ));
            valueLabel.setSize(new Dimension(VALUE_WIDTH, Short.MAX_VALUE));
            int valueHeight = Math.max(26, valueLabel.getUI().getPreferredSize(valueLabel).height);
            valueLabel.setPreferredSize(new Dimension(VALUE_WIDTH, valueHeight));
            valueLabel.setMinimumSize(new Dimension(VALUE_WIDTH, valueHeight));
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.weightx = 0.0;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            add(createWrappedText(label, LABEL_WIDTH), constraints);
            constraints.gridx = 1;
            constraints.weightx = 1.0;
            constraints.anchor = GridBagConstraints.CENTER;
            add(valueLabel, constraints);
            row++;
        }

        private void addLegend(String inputText, String infoText) {
            JPanel legend = new JPanel(new GridBagLayout());
            legend.setOpaque(false);
            GridBagConstraints legendConstraints = new GridBagConstraints();
            legendConstraints.gridy = 0;
            legendConstraints.anchor = GridBagConstraints.WEST;
            legendConstraints.insets = new Insets(0, 0, 0, 7);

            addLegendItem(legend, legendConstraints, 0, FIELD_BG, FIELD_BORDER, inputText);
            legendConstraints.gridy = 1;
            addLegendItem(legend, legendConstraints, 0, INFO_FIELD_BG, INFO_FIELD_BORDER, infoText);

            constraints.gridx = 0;
            constraints.gridy = row++;
            constraints.gridwidth = 2;
            constraints.weightx = 1.0;
            constraints.insets = new Insets(2, 8, 8, 8);
            add(legend, constraints);
            constraints.insets = new Insets(4, 8, 4, 8);
            constraints.gridwidth = 1;
        }

        private void addLegendItem(JPanel legend, GridBagConstraints constraints, int x, Color fill, Color border, String text) {
            JPanel swatch = new JPanel();
            swatch.setPreferredSize(new Dimension(22, 14));
            swatch.setBackground(fill);
            swatch.setBorder(BorderFactory.createLineBorder(border));
            constraints.gridx = x;
            legend.add(swatch, constraints);

            JLabel label = new JLabel(text);
            label.setFont(label.getFont().deriveFont(11f));
            constraints.gridx = x + 1;
            constraints.insets = new Insets(0, 0, 0, x == 0 ? 14 : 0);
            legend.add(label, constraints);
            constraints.insets = new Insets(0, 0, 0, 7);
        }

        private JTextArea createWrappedText(String text, int width) {
            JTextArea label = new JTextArea(text);
            label.setEditable(false);
            label.setFocusable(false);
            label.setLineWrap(true);
            label.setWrapStyleWord(true);
            label.setOpaque(false);
            label.setFont(UIManager.getFont("Label.font"));
            int longestWord = java.util.Arrays.stream(text.split("\\s+"))
                    .mapToInt(word -> label.getFontMetrics(label.getFont()).stringWidth(word)).max().orElse(0);
            if (longestWord > width) {
                float size = Math.max(11f, label.getFont().getSize2D() * width / longestWord);
                label.setFont(label.getFont().deriveFont(size));
            }
            label.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            label.setSize(new Dimension(width, Short.MAX_VALUE));
            int height = Math.max(20, label.getUI().getPreferredSize(label).height);
            label.setPreferredSize(new Dimension(width, height));
            label.setMinimumSize(new Dimension(width, 20));
            label.setToolTipText(text);
            return label;
        }

        private void addHint(String text) {
            JTextArea hint = createWrappedText(text, LABEL_WIDTH + VALUE_WIDTH);
            hint.setFont(hint.getFont().deriveFont(12f));
            hint.setSize(new Dimension(LABEL_WIDTH + VALUE_WIDTH, Short.MAX_VALUE));
            int height = Math.max(20, hint.getUI().getPreferredSize(hint).height) + 2;
            hint.setPreferredSize(new Dimension(LABEL_WIDTH + VALUE_WIDTH, height));
            hint.setMinimumSize(new Dimension(LABEL_WIDTH + VALUE_WIDTH, height));
            constraints.gridx = 0;
            constraints.gridy = row++;
            constraints.gridwidth = 2;
            constraints.weightx = 1.0;
            add(hint, constraints);
            constraints.gridwidth = 1;
        }

        private void addActionRow(JButton... buttons) {
            JPanel actions = new JPanel(new GridBagLayout());
            actions.setOpaque(false);
            GridBagConstraints actionConstraints = new GridBagConstraints();
            actionConstraints.fill = GridBagConstraints.HORIZONTAL;
            actionConstraints.weightx = 1.0;
            for (int i = 0; i < buttons.length; i++) {
                actionConstraints.gridx = i;
                actionConstraints.insets = new Insets(0, i == 0 ? 0 : 4, 0, i == buttons.length - 1 ? 0 : 4);
                actions.add(buttons[i], actionConstraints);
            }

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
            STACKED_BARS,
            DISCRETE_BARS,
            NORMAL_CURVE
        }

        private Mode mode = Mode.EMPTY;
        private String title = "Noch keine Berechnung ausgeführt.";
        private List<Segment> segments = List.of();
        private List<BarGroup> barGroups = List.of();
        private List<DataPoint> dataPoints = List.of();
        private CalculationService.NormalPlot normalPlot;
        private Locale locale = Locale.GERMANY;
        private final List<HitRegion> hitRegions = new ArrayList<>();
        private record HitRegion(Rectangle bounds, String text) { }

        private ChartPanel() {
            setPreferredSize(new Dimension(540, 280));
            setMinimumSize(new Dimension(320, 240));
            setBackground(Color.WHITE);
            setToolTipText("");
            clear(title);
        }

        private void clear(String message) {
            mode = Mode.EMPTY;
            title = message;
            segments = List.of();
            barGroups = List.of();
            dataPoints = List.of();
            normalPlot = null;
            hitRegions.clear();
            repaint();
        }

        private void setNormalCurve(CalculationService.NormalPlot plot, List<Segment> segments, String title) {
            this.mode = Mode.NORMAL_CURVE;
            this.normalPlot = plot;
            this.segments = segments;
            this.title = title;
            repaint();
        }

        private BufferedImage snapshot() {
            BufferedImage image = new BufferedImage(Math.max(1, getWidth() * 2), Math.max(1, getHeight() * 2), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.scale(2, 2);
            printAll(graphics);
            graphics.dispose();
            return image;
        }

        @Override public String getToolTipText(MouseEvent event) {
            if (mode == Mode.NORMAL_CURVE) {
                Rectangle bounds = normalBounds();
                if (!bounds.contains(event.getPoint())) return null;
                double z = -4 + 8.0 * (event.getX() - bounds.x) / bounds.width;
                double x = Math.fma(z, normalPlot.standardDeviation(), normalPlot.mean());
                double density = ProbabilityDistributions.normalDensity(normalPlot.mean(), normalPlot.standardDeviation(), normalPlot.mean()) * Math.exp(-0.5 * z * z);
                double cdf = ProbabilityDistributions.normalCumulative(0, 1, z);
                String position = Double.isFinite(x) ? String.format(locale, "x = %.6g", x) : String.format(locale, "z = %.6g", z);
                return String.format(locale, "<html>%s<br>f(x) = %.6g<br>Pr(X &lt;= x) = %.6g (%.4f%%)</html>", position, density, cdf, cdf * 100);
            }
            for (HitRegion region : hitRegions) if (region.bounds().contains(event.getPoint())) return region.text();
            return null;
        }

        private Rectangle normalBounds() {
            return new Rectangle(68, 54, Math.max(1, getWidth() - 106), Math.max(35, getHeight() - 134));
        }

        private void paintNormalCurve(Graphics2D g) {
            Rectangle bounds = normalBounds();
            double lower = ProbabilityDistributions.standardized(normalPlot.mean(), normalPlot.standardDeviation(), normalPlot.lower());
            double upper = ProbabilityDistributions.standardized(normalPlot.mean(), normalPlot.standardDeviation(), normalPlot.upper());
            double peak = ProbabilityDistributions.normalDensity(normalPlot.mean(), normalPlot.standardDeviation(), normalPlot.mean());
            int baseline = bounds.y + bounds.height;
            g.setFont(g.getFont().deriveFont(10f));
            g.setColor(new Color(65, 65, 65));
            g.drawString("f(x)", 16, bounds.y - 8);
            for (int tick = 0; tick <= 4; tick++) {
                int y = baseline - tick * bounds.height / 4;
                g.setColor(new Color(218, 218, 218));
                g.drawLine(bounds.x, y, bounds.x + bounds.width, y);
                g.setColor(new Color(65, 65, 65));
                String text = String.format(locale, "%.2g", peak * tick / 4);
                g.drawString(text, Math.max(4, bounds.x - g.getFontMetrics().stringWidth(text) - 6), y + 4);
            }
            Path2D curve = new Path2D.Double();
            for (int pixel = 0; pixel <= bounds.width; pixel++) {
                double z = -4 + 8.0 * pixel / bounds.width;
                double y = baseline - bounds.height * Math.exp(-0.5 * z * z);
                Color color = z < lower ? GRAPH_BLUE : z <= upper ? GRAPH_GREEN : GRAPH_RED;
                g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 115));
                g.drawLine(bounds.x + pixel, baseline, bounds.x + pixel, (int) Math.round(y));
                if (pixel == 0) curve.moveTo(bounds.x + pixel, y);
                else curve.lineTo(bounds.x + pixel, y);
            }
            g.setColor(new Color(45, 45, 45));
            g.setStroke(new BasicStroke(1.6f));
            g.draw(curve);
            g.drawLine(bounds.x, baseline, bounds.x + bounds.width, baseline);
            g.drawLine(bounds.x, bounds.y, bounds.x, baseline);
            g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{4, 4}, 0));
            for (double z : new double[]{lower, upper}) {
                if (z >= -4 && z <= 4) {
                    int x = bounds.x + (int) Math.round(bounds.width * (z + 4) / 8);
                    g.drawLine(x, bounds.y, x, baseline);
                }
            }
            g.setStroke(new BasicStroke(1f));
            boolean zAxis = !Double.isFinite(Math.fma(-4, normalPlot.standardDeviation(), normalPlot.mean()))
                    || !Double.isFinite(Math.fma(4, normalPlot.standardDeviation(), normalPlot.mean()));
            for (int z = -4; z <= 4; z += 2) {
                int x = bounds.x + bounds.width * (z + 4) / 8;
                double value = zAxis ? z : Math.fma(z, normalPlot.standardDeviation(), normalPlot.mean());
                String text = String.format(locale, "%.4g", value);
                int width = g.getFontMetrics().stringWidth(text);
                g.drawString(text, Math.max(4, Math.min(getWidth() - width - 8, x - width / 2)), baseline + 18);
            }
            g.drawString(zAxis ? "z" : "x", bounds.x + bounds.width / 2, baseline + 32);
            paintLegend(g, segments, 24, baseline + 56);
        }

        private void setSegments(List<Segment> segments, String title) {
            this.mode = Mode.SEGMENTS;
            this.title = title;
            this.segments = segments;
            this.barGroups = List.of();
            this.dataPoints = List.of();
            repaint();
        }

        private void setStackedBars(List<BarGroup> barGroups, String title) {
            this.mode = Mode.STACKED_BARS;
            this.title = title;
            this.barGroups = barGroups;
            this.segments = List.of();
            this.dataPoints = List.of();
            repaint();
        }

        private void setDiscreteBars(List<DataPoint> dataPoints, String title) {
            this.mode = Mode.DISCRETE_BARS;
            this.title = title;
            this.dataPoints = dataPoints;
            this.segments = List.of();
            this.barGroups = List.of();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBackground(g);
            hitRegions.clear();
            if (mode != Mode.EMPTY) paintTitle(g);

            if (mode == Mode.EMPTY) {
                paintEmpty(g);
            } else if (mode == Mode.SEGMENTS) {
                paintSegmentBar(g, segments, 62, getHeight() / 2 - 16, getWidth() - 124, 34);
                paintLegend(g, segments, 62, getHeight() / 2 + 72);
            } else if (mode == Mode.STACKED_BARS) {
                paintStackedBars(g);
            } else if (mode == Mode.NORMAL_CURVE) {
                paintNormalCurve(g);
            } else {
                paintDiscreteBars(g);
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
            paintWrappedText(g, title, 22, 24, getWidth() - 44);
        }

        private void paintEmpty(Graphics2D g) {
            g.setColor(new Color(105, 105, 105));
            g.setFont(g.getFont().deriveFont(13f));
            paintWrappedText(g, title, 24, getHeight() / 2, getWidth() - 48);
        }

        private void paintWrappedText(Graphics2D g, String text, int x, int y, int width) {
            StringBuilder line = new StringBuilder();
            for (String word : text.split(" ")) {
                if (!line.isEmpty() && g.getFontMetrics().stringWidth(line + " " + word) > width) {
                    g.drawString(line.toString(), x, y);
                    y += g.getFontMetrics().getHeight();
                    line.setLength(0);
                }
                if (!line.isEmpty()) line.append(' ');
                line.append(word);
            }
            g.drawString(line.toString(), x, y);
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

        private void paintDiscreteBars(Graphics2D g) {
            if (dataPoints.isEmpty()) {
                paintEmpty(g);
                return;
            }

            int chartX = 54;
            int chartY = 58;
            int chartWidth = getWidth() - 92;
            int chartHeight = Math.max(90, getHeight() - 128);
            double maxValue = dataPoints.stream().mapToDouble(DataPoint::value).max().orElse(1.0);
            if (!(maxValue > 0)) maxValue = 1;
            int gap = dataPoints.size() > 24 ? 1 : 3;
            double slot = (double) chartWidth / dataPoints.size();
            int barWidth = Math.max(1, (int) slot - gap);

            g.setColor(new Color(225, 225, 225));
            for (int i = 0; i <= 4; i++) {
                int y = chartY + chartHeight - (chartHeight * i / 4);
                g.drawLine(chartX, y, chartX + chartWidth, y);
                g.setColor(new Color(90, 90, 90));
                g.drawString("%.2f".formatted(maxValue * i / 4.0), 12, y + 4);
                g.setColor(new Color(225, 225, 225));
            }

            for (int i = 0; i < dataPoints.size(); i++) {
                DataPoint point = dataPoints.get(i);
                int x = chartX + (int) Math.round(i * slot);
                int height = (int) Math.round(chartHeight * (point.value() / maxValue));
                int y = chartY + chartHeight - height;
                g.setColor(point.color());
                g.fillRect(x, y, barWidth, height);
                hitRegions.add(new HitRegion(new Rectangle(x, y, barWidth, Math.max(5, height)),
                        String.format(locale, "%s: %.8g (%.4f%%)", point.label(), point.value(), point.value() * 100)));
                g.setColor(new Color(80, 80, 80));
                g.drawRect(x, y, barWidth, height);
            }

            g.setColor(new Color(60, 60, 60));
            g.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight);
            g.drawLine(chartX, chartY, chartX, chartY + chartHeight);
            g.setFont(g.getFont().deriveFont(10f));
            int labelStep = Math.max(1, (int) Math.ceil(dataPoints.size() / 5.0));
            for (int i = 0; i < dataPoints.size(); i += labelStep) {
                int x = chartX + (int) Math.round(i * slot);
                String label = dataPoints.get(i).label();
                g.drawString(label, Math.min(x, chartX + chartWidth - g.getFontMetrics().stringWidth(label)), chartY + chartHeight + 16);
            }
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
                hitRegions.add(new HitRegion(new Rectangle(currentX, y, Math.max(0, segmentWidth), height),
                        String.format(locale, "%s: %.8g (%.4f%%)", segment.label(), segment.value(), segment.value() * 100)));
                g.setColor(Color.WHITE);
                if (segmentWidth > 58) {
                    g.drawString(String.format(locale, "%.1f%%", segment.value() * 100.0), currentX + 6, y + height - 9);
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
                int itemWidth = g.getFontMetrics().stringWidth(segment.label()) + 32;
                if (currentX + itemWidth > getWidth() - 24) {
                    currentX = x;
                    currentY += 20;
                }
                g.setColor(segment.color());
                g.fillRect(currentX, currentY - 10, 12, 12);
                g.setColor(new Color(40, 40, 40));
                g.drawString(segment.label(), currentX + 17, currentY);
                hitRegions.add(new HitRegion(new Rectangle(currentX, currentY - 12, itemWidth, 16),
                        String.format(locale, "%s: %.8g (%.4f%%)", segment.label(), segment.value(), segment.value() * 100)));
                currentX += Math.max(110, itemWidth);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProbabilityCalculatorGUI().setVisible(true));
    }
}
