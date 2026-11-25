import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/**
 * Dijkstra Simulator - Interactive Shortest Path Learning Platform
 * Single-file Java Swing educational application.
 * Compatible with Java 8. No external libraries required.
 */
public class DijkstraSimulator extends JFrame {
    private static final Color NAVY = new Color(25, 45, 82);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color LIGHT_BLUE = new Color(239, 246, 255);
    private static final Color BG = new Color(246, 248, 252);
    private static final Color TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final long INF = Long.MAX_VALUE;

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<String, JButton>();
    private String currentPage = "Dashboard";

    // Simulator state
    private final ArrayList<String> routers = new ArrayList<String>();
    private final ArrayList<Edge> edges = new ArrayList<Edge>();
    private NetworkPanel networkPanel;
    private JTable linkTable;
    private JTable routeTable;
    private JComboBox<String> sourceBox;
    private JTextField routerCountField, fromField, toField, costField;
    private JLabel simulatorStatus;
    private JButton runButton;

    // Practice and quiz state
    private JTextField practiceAnswer;
    private JTextArea practiceFeedback;
    private int quizIndex = 0;
    private int quizScore = 0;
    private int quizAnswered = 0;
    private JTextArea quizQuestionLabel;
    private JTextArea quizFeedbackLabel;
    private JLabel quizScoreLabel;
    private JRadioButton[] quizOptions;
    private ButtonGroup quizGroup;
    private JButton quizSubmitButton, quizNextButton;
    private boolean quizCurrentAnswered = false;

    public DijkstraSimulator() {
        setTitle("Dijkstra Simulator - Interactive Shortest Path Learning Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1320, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        buildShell();
        showPage("Dashboard");
    }

    private void buildShell() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(238, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));
        JPanel sidebarMenu = new JPanel();
        sidebarMenu.setBackground(Color.WHITE);
        sidebarMenu.setLayout(new BoxLayout(sidebarMenu, BoxLayout.Y_AXIS));
        JPanel navigation = new JPanel(new GridLayout(5, 1));
        navigation.setBackground(Color.WHITE);

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setBackground(Color.WHITE);
        brand.setBorder(new EmptyBorder(8, 18, 8, 12));
        JPanel brandText = new JPanel();
        brandText.setBackground(Color.WHITE);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel brandTitle = new JLabel("Dijkstra Simulator");
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        brandTitle.setForeground(NAVY);
        JLabel brandSub = new JLabel("LEARNING PLATFORM");
        brandSub.setFont(new Font("SansSerif", Font.BOLD, 9));
        brandSub.setForeground(MUTED);
        brandText.add(brandTitle);
        brandText.add(Box.createVerticalStrut(4));
        brandText.add(brandSub);
        brand.add(brandText, BorderLayout.CENTER);
        sidebarMenu.add(brand);

        addNav(navigation, "Dashboard", "⌂");
        addNav(navigation, "Learn", "▤");
        addNav(navigation, "Simulator", "◇");
        addNav(navigation, "Practice & Quiz", "✓");
        addNav(navigation, "Reference", "▣");

        sidebar.add(sidebarMenu, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(createTopBar(), BorderLayout.NORTH);
        pageHost.setBackground(BG);
        pageHost.add(createDashboardPage(), "Dashboard");
        pageHost.add(createLearnPage(), "Learn");
        pageHost.add(createSimulatorPage(), "Simulator");
        pageHost.add(createPracticeQuizPage(), "Practice & Quiz");
        pageHost.add(createReferencePage(), "Reference");
        main.add(pageHost, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);
        add(main, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(14, 26, 14, 26)));
        JLabel section = new JLabel("INTERACTIVE LEARNING ENVIRONMENT");
        section.setFont(new Font("SansSerif", Font.BOLD, 11));
        section.setForeground(MUTED);
        top.add(section, BorderLayout.WEST);
        return top;
    }

    private void addNav(JPanel sidebar, final String name, String symbol) {
        final JButton button = new JButton("  " + symbol + "     " + name);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setForeground(TEXT);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { showPage(name); }
        });
        navButtons.put(name, button);
        sidebar.add(button);
    }

    private void showPage(String name) {
        currentPage = name;
        pageLayout.show(pageHost, name);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean selected = entry.getKey().equals(name);
            entry.getValue().setBackground(selected ? LIGHT_BLUE : Color.WHITE);
            entry.getValue().setForeground(selected ? BLUE : TEXT);
            entry.getValue().setFont(new Font("SansSerif", selected ? Font.BOLD : Font.PLAIN, 13));
        }
    }

    // ---------------- Dashboard ----------------
    private JPanel createDashboardPage() {
        JPanel page = pagePanel();
        page.setLayout(new BorderLayout(0, 18));
        page.add(pageHeading("Welcome to Dijkstra Simulator", "An interactive platform to understand, visualize and practice shortest-path routing."), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        JPanel hero = new JPanel(new BorderLayout(20, 10));
        hero.setBackground(NAVY);
        hero.setBorder(new EmptyBorder(26, 28, 26, 28));
        JTextArea heroTitle = wrappedDisplayText("Explore how networks find the shortest path", 2, new Font("SansSerif", Font.BOLD, 23), Color.WHITE);
        JTextArea heroDesc = wrappedDisplayText("Learn the concepts, build a network, watch the algorithm work, and test your understanding.", 2, new Font("SansSerif", Font.PLAIN, 13), new Color(219, 231, 250));
        heroTitle.setBackground(NAVY);
        heroDesc.setBackground(NAVY);
        JButton start = primaryButton("Open Simulator");
        start.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { showPage("Simulator"); } });
        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        heroText.add(heroTitle);
        heroText.add(Box.createVerticalStrut(10));
        heroText.add(heroDesc);
        heroText.add(Box.createVerticalStrut(18));
        heroText.add(start);
        hero.add(heroText, BorderLayout.CENTER);
        body.add(hero, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 2, 12, 12));
        cards.setOpaque(false);
        cards.add(featureCard("01", "Learn", "Understand topics like graph theory, routing and Dijkstra's algorithm.", "Open lessons", "Learn"));
        cards.add(featureCard("02", "Simulator", "Create weighted networks and calculate optimal routes.", "Build a network", "Simulator"));
        cards.add(featureCard("03", "Practice & Quiz", "Solve shortest-path questions and check your understanding.", "Test yourself", "Practice & Quiz"));
        cards.add(featureCard("04", "Reference", "Review key terms, formulas, pseudocode and complexity.", "View reference", "Reference"));
        body.add(cards, BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel featureCard(String number, String title, String description, String action, final String destination) {
        JPanel card = cardPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel num = new JLabel(number); num.setFont(new Font("SansSerif", Font.BOLD, 12)); num.setForeground(BLUE); num.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel heading = new JLabel(title); heading.setFont(new Font("SansSerif", Font.BOLD, 17)); heading.setForeground(NAVY); heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea desc = wrappedText(description, 3); desc.setBackground(Color.WHITE); desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton link = new JButton(action + "  →"); link.setForeground(BLUE); link.setFont(new Font("SansSerif", Font.BOLD, 11)); link.setBorderPainted(false); link.setContentAreaFilled(false); link.setFocusPainted(false); link.setHorizontalAlignment(SwingConstants.LEFT); link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(Component.LEFT_ALIGNMENT);
        link.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { showPage(destination); } });
        card.add(num); card.add(Box.createVerticalStrut(10)); card.add(heading); card.add(Box.createVerticalStrut(8)); card.add(desc); card.add(Box.createVerticalGlue()); card.add(link);
        return card;
    }

    // ---------------- Learn ----------------
    private JPanel createLearnPage() {
        JPanel page = pagePanel(); page.setLayout(new BorderLayout(0, 16));
        page.add(pageHeading("Learn the foundations", "Follow the lessons in order or select a topic to revise a specific concept."), BorderLayout.NORTH);
        final String[] topics = {"1. Computer Networks", "2. Graph Theory", "3. Routing Fundamentals", "4. Link-State Routing", "5. Dijkstra's Algorithm", "6. Routing Tables", "7. Complexity & Applications"};
        final String[] titles = {"Computer Networks", "Graph Theory", "Routing Fundamentals", "Link-State Routing", "Dijkstra's Algorithm", "Routing Table Generation", "Complexity and Real-World Applications"};
        final String[] content = {
            "A computer network is a collection of interconnected devices that exchange data. Devices communicate through links and use agreed protocols to move information from one point to another. Routers connect different networks and choose where packets should be forwarded.\n\nIn this simulator, each router is represented as a vertex and each communication link is represented as an edge. The link cost is a numerical value that represents the relative cost of using that connection.",
            "A graph is a mathematical structure made of vertices (also called nodes) and edges that connect pairs of vertices. A weighted graph assigns a value or weight to each edge.\n\nFor this project, routers are vertices and network links are weighted edges. The graph is undirected because a link can be traversed in either direction with the same cost. A path is a sequence of connected vertices. The path cost is the sum of its edge weights.",
            "Routing is the process of selecting a path for data to travel through a network. A router uses routing information to decide the next hop for a destination.\n\nA routing metric is a value used to compare possible paths. In this simulator, the metric is the sum of positive link costs. The route with the lowest total cost is selected as the shortest path.",
            "Link-state routing is a routing approach in which routers build a view of the network topology from link-state information. A shortest-path algorithm can then be applied to that topology to calculate routes.\n\nThis application demonstrates the centralized calculation of shortest paths from one selected source router. It is an educational model of the link-state shortest-path calculation, not a live distributed routing protocol or packet-forwarding system.",
            "Dijkstra's algorithm finds shortest paths from one source vertex to all reachable vertices in a graph with non-negative edge weights. It is a greedy algorithm: at each iteration it selects the unvisited vertex with the smallest known tentative distance.\n\nThe selected distance becomes final. The algorithm then checks its neighbors and updates a neighbor's distance when a cheaper route through the selected vertex is found. This update is called relaxation.",
            "A routing table summarizes the best known route from a source router to each destination. The destination identifies where traffic is intended to go. The total cost gives the path metric, the next hop identifies the first router to visit, and the optimal route shows the complete path.\n\nIf a destination cannot be reached from the source, the table should identify that no route exists rather than presenting a misleading finite cost.",
            "With an adjacency matrix and a linear scan to select the next minimum-distance vertex, this implementation of Dijkstra's algorithm takes O(V²) time. The adjacency matrix uses O(V²) space. V represents the number of vertices (routers).\n\nDijkstra's algorithm requires non-negative edge weights. It is used in shortest-path problems such as network routing, road navigation and communication-network planning. For sparse graphs with many vertices, priority-queue implementations can be more efficient."
        };
        JPanel contentPanel = cardPanel(); contentPanel.setLayout(new BorderLayout(12, 12));
        final JLabel lessonTitle = new JLabel(titles[0]); lessonTitle.setFont(new Font("SansSerif", Font.BOLD, 21)); lessonTitle.setForeground(NAVY);
        final JTextArea lessonText = new JTextArea(content[0]); lessonText.setEditable(false); lessonText.setLineWrap(true); lessonText.setWrapStyleWord(true); lessonText.setFont(new Font("SansSerif", Font.PLAIN, 14)); lessonText.setForeground(TEXT); lessonText.setBackground(Color.WHITE); lessonText.setBorder(new EmptyBorder(10, 2, 10, 2));
        final JLabel lessonCount = new JLabel("LESSON 1 OF 7"); lessonCount.setFont(new Font("SansSerif", Font.BOLD, 10)); lessonCount.setForeground(BLUE);
        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel outline = new JLabel("COURSE CONTENT"); outline.setFont(new Font("SansSerif", Font.BOLD, 10)); outline.setForeground(MUTED); outline.setBorder(new EmptyBorder(0, 0, 10, 0)); left.add(outline);
        final JList<String> topicList = new JList<String>(topics); topicList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); topicList.setSelectedIndex(0); topicList.setFont(new Font("SansSerif", Font.PLAIN, 12)); topicList.setFixedCellHeight(42); topicList.setBorder(new EmptyBorder(4, 4, 4, 4)); topicList.setBackground(new Color(250, 251, 253));
        topicList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) { int i = topicList.getSelectedIndex(); if (i >= 0) { lessonTitle.setText(titles[i]); lessonText.setText(content[i]); lessonCount.setText("LESSON " + (i + 1) + " OF 7"); lessonText.setCaretPosition(0); } } });
        left.add(new JScrollPane(topicList)); left.setPreferredSize(new Dimension(235, 0));
        JPanel lessonBody = new JPanel(new BorderLayout(0, 14)); lessonBody.setOpaque(false); lessonBody.add(lessonCount, BorderLayout.NORTH); lessonBody.add(lessonTitle, BorderLayout.CENTER);
        JPanel textWrap = new JPanel(new BorderLayout()); textWrap.setBackground(Color.WHITE); textWrap.setBorder(new EmptyBorder(18, 18, 18, 18)); textWrap.add(lessonText, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout(0, 12)); right.setOpaque(false); right.add(lessonBody, BorderLayout.NORTH); right.add(textWrap, BorderLayout.CENTER);
        contentPanel.add(left, BorderLayout.WEST); contentPanel.add(right, BorderLayout.CENTER);
        page.add(contentPanel, BorderLayout.CENTER); return page;
    }

    // ---------------- Simulator ----------------
    private JPanel createSimulatorPage() {
        JPanel page = pagePanel(); page.setLayout(new BorderLayout(0, 16));
        page.add(pageHeading("Interactive network simulator", "Create a weighted network, choose a source router and observe Dijkstra's algorithm."), BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(12, 0)); content.setOpaque(false);
        JPanel controls = cardPanel(); controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        routerCountField = new JTextField(5); routerCountField.setPreferredSize(new Dimension(82, 28));
        fromField = new JTextField(4); fromField.setPreferredSize(new Dimension(68, 28));
        toField = new JTextField(4); toField.setPreferredSize(new Dimension(68, 28));
        costField = new JTextField(5); costField.setPreferredSize(new Dimension(82, 28));
        sourceBox = new JComboBox<String>(); sourceBox.setPreferredSize(new Dimension(104, 28));
        JLabel setupHeading = new JLabel("Network setup"); setupHeading.setFont(new Font("SansSerif",Font.BOLD,15)); setupHeading.setForeground(NAVY); setupHeading.setAlignmentX(Component.LEFT_ALIGNMENT); controls.add(setupHeading);
        GridBagConstraints inputCell = new GridBagConstraints(); inputCell.insets = new Insets(3, 4, 3, 4); inputCell.anchor = GridBagConstraints.WEST; inputCell.fill = GridBagConstraints.HORIZONTAL;
        JPanel createRow = new JPanel(new GridBagLayout()); createRow.setOpaque(false); createRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCell.gridy=0;inputCell.gridx=0;inputCell.weightx=0;createRow.add(fieldLabel("No. of Routers:"),inputCell);
        inputCell.gridx=1;inputCell.weightx=1;createRow.add(routerCountField,inputCell);
        JButton create = primaryButton("Create Network"); create.addActionListener(e -> createNetwork()); inputCell.gridx=2;inputCell.weightx=0;createRow.add(create,inputCell); controls.add(createRow);
        JPanel linkRow = new JPanel(new GridBagLayout()); linkRow.setOpaque(false); linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCell.gridx=0;inputCell.weightx=0;linkRow.add(fieldLabel("From:"),inputCell);
        inputCell.gridx=1;inputCell.weightx=1;linkRow.add(fromField,inputCell);
        inputCell.gridx=3;inputCell.weightx=0;linkRow.add(fieldLabel("To:"),inputCell);
        inputCell.gridx=4;inputCell.weightx=1;linkRow.add(toField,inputCell); controls.add(linkRow);
        JPanel costRow = new JPanel(new GridBagLayout()); costRow.setOpaque(false); costRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCell.gridx=0;inputCell.weightx=0;costRow.add(fieldLabel("Cost:"),inputCell);
        inputCell.gridx=1;inputCell.weightx=1;costRow.add(costField,inputCell);
        JButton add = secondaryButton("Add Link"); add.addActionListener(e -> addLink()); inputCell.gridx=2;inputCell.weightx=0;costRow.add(add,inputCell); controls.add(costRow);
        JPanel sourceRow = new JPanel(new GridBagLayout()); sourceRow.setOpaque(false); sourceRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCell.gridx=0;inputCell.weightx=0;sourceRow.add(fieldLabel("Source router:"),inputCell);
        inputCell.gridx=1;inputCell.weightx=1;sourceRow.add(sourceBox,inputCell); controls.add(sourceRow);
        JPanel actionRow = new JPanel(new GridBagLayout()); actionRow.setOpaque(false); actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        runButton = primaryButton("Run Dijkstra"); runButton.addActionListener(e -> runDijkstra()); inputCell.gridx=0;inputCell.weightx=0;inputCell.fill=GridBagConstraints.NONE;actionRow.add(runButton,inputCell);
        inputCell.gridx=1;inputCell.weightx=1;inputCell.fill=GridBagConstraints.HORIZONTAL;actionRow.add(Box.createHorizontalGlue(),inputCell);
        JButton reset = secondaryButton("Reset"); reset.addActionListener(e -> resetSimulator()); inputCell.gridx=2;inputCell.weightx=0;inputCell.fill=GridBagConstraints.NONE;actionRow.add(reset,inputCell); controls.add(actionRow);

        JPanel graphCard = cardPanel(); graphCard.setLayout(new BorderLayout(0, 8)); graphCard.add(sectionTitle("Network topology", "Blue edges indicate links in the current optimal routes."), BorderLayout.NORTH);
        networkPanel = new NetworkPanel(); networkPanel.setPreferredSize(new Dimension(440, 340)); graphCard.add(networkPanel, BorderLayout.CENTER);
        JPanel linksCard = cardPanel(); linksCard.setLayout(new BorderLayout(0, 6)); linksCard.add(sectionTitle("Link-state information", "All configured undirected links."), BorderLayout.NORTH);
        linkTable = createTable(new String[]{"From", "To", "Cost"}); linksCard.add(new JScrollPane(linkTable), BorderLayout.CENTER);
        JButton deleteLink = secondaryButton("Delete Selected Link"); deleteLink.addActionListener(e -> deleteSelectedLink());
        JPanel linkActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); linkActions.setOpaque(false); linkActions.add(deleteLink);
        linksCard.add(linkActions, BorderLayout.SOUTH);
        JPanel routesCard = cardPanel(); routesCard.setLayout(new BorderLayout(0, 6)); routesCard.add(sectionTitle("Routing table", "Shortest route from the selected source."), BorderLayout.NORTH);
        routeTable = createTable(new String[]{"Destination", "Cost", "Next Hop", "Optimal Route"}); routesCard.add(new JScrollPane(routeTable), BorderLayout.CENTER);
        JPanel tables = new JPanel(new GridLayout(2, 1, 5, 10));
        tables.setOpaque(false);
        tables.add(linksCard);
        tables.add(routesCard);
        JPanel leftColumn = new JPanel(new BorderLayout(0, 10)); leftColumn.setOpaque(false);
        leftColumn.add(controls, BorderLayout.NORTH);
        leftColumn.add(graphCard, BorderLayout.CENTER);
        JPanel work = new JPanel(new GridBagLayout()); work.setOpaque(false);
        GridBagConstraints workCell = new GridBagConstraints(); workCell.gridy=0; workCell.fill=GridBagConstraints.BOTH; workCell.weighty=1;
        workCell.gridx=0; workCell.weightx=0.38; workCell.insets=new Insets(0,0,0,8); work.add(leftColumn,workCell);
        workCell.gridx=1; workCell.weightx=0.62; workCell.insets=new Insets(0,8,0,0); work.add(tables,workCell);
        content.add(work, BorderLayout.CENTER);

        simulatorStatus = new JLabel("Status: Create a network to begin."); simulatorStatus.setForeground(MUTED); simulatorStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel statusPanel = new JPanel(new BorderLayout()); statusPanel.setOpaque(false); statusPanel.add(simulatorStatus, BorderLayout.WEST);
        content.add(statusPanel, BorderLayout.SOUTH);
        page.add(content, BorderLayout.CENTER);
        sourceBox.addActionListener(e -> { if (routeTable != null && routeTable.getRowCount() > 0) invalidateResults("Source changed. Run Dijkstra again."); });
        return page;
    }

    private boolean createNetwork() {
        try {
            int number = Integer.parseInt(routerCountField.getText().trim());
            if (number < 2 || number > 15) {
                error("Enter a number of routers between 2 and 15.");
                return false;
            }
            routers.clear();
            edges.clear();
            sourceBox.removeAllItems();
            for (int i = 0; i < number; i++) {
                String router = String.valueOf((char) ('A' + i));
                routers.add(router);
                sourceBox.addItem(router);
            }
            clearTables();
            networkPanel.clearPaths();
            networkPanel.repaint();
            simulatorStatus.setText("Network created. Add links between routers.");
            return true;
        } catch (NumberFormatException ex) {
            error("Enter a valid number of routers.");
            return false;
        }
    }

    private boolean addLink() {
        if (routers.isEmpty()) { error("Create the network first."); return false; }
        String from = fromField.getText().trim().toUpperCase();
        String to = toField.getText().trim().toUpperCase();
        if (!routers.contains(from) || !routers.contains(to)) { error("Enter valid router names."); return false; }
        if (from.equals(to)) { error("A router cannot connect to itself."); return false; }
        long cost;
        try { cost = Long.parseLong(costField.getText().trim()); }
        catch (NumberFormatException ex) { error("Enter a valid positive whole-number link cost."); return false; }
        if (cost <= 0 || cost == INF) { error("Link cost must be a positive number smaller than Long.MAX_VALUE."); return false; }
        for (Edge edge : edges) {
            if ((edge.from.equals(from) && edge.to.equals(to)) || (edge.from.equals(to) && edge.to.equals(from))) {
                error("This link already exists.");
                return false;
            }
        }
        edges.add(new Edge(from, to, cost));
        ((DefaultTableModel) linkTable.getModel()).addRow(new Object[]{from, to, cost});
        fromField.setText("");
        toField.setText("");
        costField.setText("");
        invalidateResults("Link added. Run Dijkstra to calculate routes.");
        networkPanel.repaint();
        return true;
    }

    private void deleteSelectedLink() {
        int selectedRow = linkTable.getSelectedRow();
        if (selectedRow < 0) {
            error("Select a link row to delete.");
            return;
        }
        int modelRow = linkTable.convertRowIndexToModel(selectedRow);
        ((DefaultTableModel) linkTable.getModel()).removeRow(modelRow);
        edges.remove(modelRow);
        invalidateResults("Link deleted. Run Dijkstra to calculate routes again.");
        networkPanel.repaint();
    }

    private void runDijkstra() {
        if (routers.isEmpty()) { error("Create the network first."); return; }
        if (edges.isEmpty()) { error("Add network links first."); return; }
        String source = (String) sourceBox.getSelectedItem();
        int n = routers.size();
        long[][] graph = new long[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(graph[i], INF);
            graph[i][i] = 0;
        }
        for (Edge edge : edges) {
            int u = routers.indexOf(edge.from);
            int v = routers.indexOf(edge.to);
            graph[u][v] = edge.cost;
            graph[v][u] = edge.cost;
        }
        int sourceIndex = routers.indexOf(source);
        long[] distance = new long[n];
        int[] previous = new int[n];
        boolean[] visited = new boolean[n];
        Arrays.fill(distance, INF);
        Arrays.fill(previous, -1);
        distance[sourceIndex] = 0;
        for (int count = 0; count < n; count++) {
            int current = -1;
            long minimum = INF;
            for (int i = 0; i < n; i++) {
                if (!visited[i] && distance[i] < minimum) {
                    minimum = distance[i];
                    current = i;
                }
            }
            if (current == -1) break;
            visited[current] = true;
            for (int neighbor = 0; neighbor < n; neighbor++) {
                if (visited[neighbor] || graph[current][neighbor] == INF || distance[current] == INF) continue;
                if (distance[current] >= INF - graph[current][neighbor]) continue;
                long newDistance = distance[current] + graph[current][neighbor];
                if (newDistance < distance[neighbor]) {
                    distance[neighbor] = newDistance;
                    previous[neighbor] = current;
                }
            }
        }
        Result result = new Result(source, distance, previous);
        populateRoutingTable(result);
        networkPanel.setOptimalPaths(buildAllPaths(result));
        networkPanel.setSourceRouter(source);
        networkPanel.repaint();
        simulatorStatus.setText("Status: Dijkstra completed successfully.");
    }

    private void populateRoutingTable(Result result) {
        DefaultTableModel model = (DefaultTableModel) routeTable.getModel();
        model.setRowCount(0);
        for (int i = 0; i < routers.size(); i++) {
            if (result.distance[i] == INF) {
                model.addRow(new Object[]{routers.get(i), "∞", "—", "No route"});
                continue;
            }
            List<String> path = reconstructPath(i, result.previous);
            if (path.isEmpty()) {
                model.addRow(new Object[]{routers.get(i), "∞", "—", "No route"});
                continue;
            }
            model.addRow(new Object[]{routers.get(i), result.distance[i], path.size() > 1 ? path.get(1) : "—", joinPath(path)});
        }
    }

    private ArrayList<ArrayList<String>> buildAllPaths(Result result) {
        ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < routers.size(); i++) {
            if (result.distance[i] != INF) paths.add(new ArrayList<String>(reconstructPath(i, result.previous)));
        }
        return paths;
    }

    private List<String> reconstructPath(int destination, int[] previous) {
        ArrayList<String> path = new ArrayList<String>();
        int current = destination;
        int steps = 0;
        while (current != -1 && steps < routers.size()) {
            if (current < 0 || current >= routers.size()) return Collections.emptyList();
            path.add(routers.get(current));
            current = previous[current];
            steps++;
        }
        if (current != -1) return Collections.emptyList();
        Collections.reverse(path);
        return path;
    }

    private String joinPath(List<String> path) { StringBuilder text = new StringBuilder(); for (String router : path) { if (text.length() > 0) text.append(" → "); text.append(router); } return text.toString(); }

    private void clearTables() { clearTable(linkTable); clearTable(routeTable); }

    private void invalidateResults(String message) {
        if (routeTable != null) clearTable(routeTable);
        if (networkPanel != null) { networkPanel.clearPaths(); networkPanel.setSourceRouter(null); networkPanel.repaint(); }
        if (simulatorStatus != null) simulatorStatus.setText(message);
    }

    private void resetSimulator() {
        routerCountField.setText("");
        fromField.setText("");
        toField.setText("");
        costField.setText("");
        routers.clear();
        edges.clear();
        sourceBox.removeAllItems();
        clearTables();
        networkPanel.clearPaths();
        networkPanel.repaint();
        simulatorStatus.setText("Network reset. Create a new network to begin.");
    }

    // ---------------- Practice & Quiz ----------------
    private JPanel createPracticeQuizPage() {
        JPanel page=pagePanel();page.setLayout(new BorderLayout(0,16));page.add(pageHeading("Practice your understanding","Try a shortest-path exercise, then take the knowledge-check quiz."),BorderLayout.NORTH);
        JPanel body=new JPanel(new GridLayout(1,2,14,0));body.setOpaque(false);
        JPanel practice=cardPanel();practice.setLayout(new BoxLayout(practice,BoxLayout.Y_AXIS));practice.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);practice.add(sectionTitle("Practice challenge","Use the weighted graph below to calculate a shortest-path cost."));
        JTextArea graphText=new JTextArea("Undirected weighted links\n\nA — B : 4\nA — C : 2\nC — B : 1\nB — D : 5\nC — D : 8\nD — E : 2\nB — E : 7\n\nQuestion: What is the minimum cost from A to D?");graphText.setEditable(false);graphText.setLineWrap(true);graphText.setWrapStyleWord(true);graphText.setFont(new Font("Monospaced",Font.PLAIN,13));graphText.setForeground(TEXT);graphText.setBackground(new Color(250,251,253));graphText.setBorder(new EmptyBorder(14,14,14,14));graphText.setAlignmentX(Component.LEFT_ALIGNMENT);
        practice.add(Box.createVerticalStrut(12));practice.add(graphText);practice.add(Box.createVerticalStrut(12));practice.add(fieldLabel("Your answer (total cost)"));practice.add(Box.createVerticalStrut(4));practiceAnswer=new JTextField(10);practiceAnswer.setMaximumSize(new Dimension(180,32));practiceAnswer.setAlignmentX(Component.LEFT_ALIGNMENT);practice.add(practiceAnswer);practice.add(Box.createVerticalStrut(8));JButton check=primaryButton("Check Answer");check.setAlignmentX(Component.LEFT_ALIGNMENT);practice.add(check);practiceFeedback=new JTextArea(" ",2,24);practiceFeedback.setEditable(false);practiceFeedback.setFocusable(false);practiceFeedback.setLineWrap(true);practiceFeedback.setWrapStyleWord(true);practiceFeedback.setOpaque(false);practiceFeedback.setFont(new Font("SansSerif",Font.BOLD,12));practiceFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);practice.add(Box.createVerticalStrut(8));practice.add(practiceFeedback);
        ActionListener checkPracticeAnswer = e -> {
            String answer = practiceAnswer.getText().trim();
            try {
                long value = Long.parseLong(answer);
                if (value == 8) {
                    practiceFeedback.setForeground(new Color(22,128,74));
                    practiceFeedback.setText("Correct. A → C → B → D has total cost 2 + 1 + 5 = 8.");
                } else {
                    practiceFeedback.setForeground(new Color(185,65,65));
                    practiceFeedback.setText("Not quite. Compare the alternative routes and add their edge costs.");
                }
            } catch (NumberFormatException ex) {
                practiceFeedback.setForeground(new Color(185,65,65));
                practiceFeedback.setText("Enter a whole-number cost.");
            }
        };
        check.addActionListener(checkPracticeAnswer);
        practiceAnswer.addActionListener(checkPracticeAnswer);
        JPanel quiz=cardPanel();quiz.setLayout(new BoxLayout(quiz,BoxLayout.Y_AXIS));quiz.add(sectionTitle("Knowledge check","Answer one multiple-choice question at a time."));quizScoreLabel=new JLabel("Score: 0 / 0");quizScoreLabel.setForeground(BLUE);quizScoreLabel.setFont(new Font("SansSerif",Font.BOLD,12));quizScoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);quiz.add(Box.createVerticalStrut(10));quiz.add(quizScoreLabel);quizQuestionLabel=new JTextArea(2,24);quizQuestionLabel.setEditable(false);quizQuestionLabel.setLineWrap(true);quizQuestionLabel.setWrapStyleWord(true);quizQuestionLabel.setOpaque(false);quizQuestionLabel.setFont(new Font("SansSerif",Font.BOLD,14));quizQuestionLabel.setForeground(NAVY);quizQuestionLabel.setBorder(new EmptyBorder(4,0,6,0));quizQuestionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);quiz.add(quizQuestionLabel);
        quizOptions=new JRadioButton[4];quizGroup=new ButtonGroup();for(int i=0;i<4;i++){quizOptions[i]=new JRadioButton();quizOptions[i].setBackground(Color.WHITE);quizOptions[i].setFont(new Font("SansSerif",Font.PLAIN,12));quizOptions[i].setForeground(TEXT);quizOptions[i].setAlignmentX(Component.LEFT_ALIGNMENT);quizGroup.add(quizOptions[i]);quiz.add(quizOptions[i]);quiz.add(Box.createVerticalGlue());}
        quiz.add(Box.createVerticalStrut(10));quizFeedbackLabel=new JTextArea(" ",3,24);quizFeedbackLabel.setEditable(false);quizFeedbackLabel.setFocusable(false);quizFeedbackLabel.setLineWrap(true);quizFeedbackLabel.setWrapStyleWord(true);quizFeedbackLabel.setOpaque(false);quizFeedbackLabel.setFont(new Font("SansSerif",Font.PLAIN,12));quizFeedbackLabel.setForeground(MUTED);quizFeedbackLabel.setBorder(new EmptyBorder(8,0,8,0));quizFeedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);quiz.add(quizFeedbackLabel);
        JPanel quizButtons=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));quizButtons.setOpaque(false);quizButtons.setAlignmentX(Component.LEFT_ALIGNMENT);quizSubmitButton=primaryButton("Submit Answer");quizNextButton=secondaryButton("Next Question");quizNextButton.setEnabled(false);quizButtons.add(quizSubmitButton);quizButtons.add(quizNextButton);quiz.add(quizButtons);
        quizSubmitButton.addActionListener(e->submitQuizAnswer());quizNextButton.addActionListener(e->nextQuizQuestion());loadQuizQuestion();
        body.add(practice);body.add(quiz);page.add(body,BorderLayout.CENTER);return page;
    }

    private final QuizQuestion[] questions={
        new QuizQuestion("What does a router represent in the network graph?",new String[]{"A vertex","A link cost","A route table","A destination only"},0,"Each router is shown as a vertex in the graph."),
        new QuizQuestion("What does a link connect?",new String[]{"Two routers","Two costs","Two route tables","A router to itself"},0,"A link is an edge that connects two routers."),
        new QuizQuestion("What does the link cost tell us?",new String[]{"How many routers there are","How expensive it is to use the link","Which router is the source","How many links exist"},1,"The cost is the weight assigned to using a link."),
        new QuizQuestion("Can standard Dijkstra's algorithm use a negative link cost?",new String[]{"Yes, always","No, costs must be non-negative","Only if the graph is small","Only for the source router"},1,"Dijkstra's algorithm requires non-negative link costs."),
        new QuizQuestion("When a cheaper route is found, what should happen to the distance?",new String[]{"Update it to the cheaper cost","Delete the router","Leave it unchanged","Set it to zero"},0,"The distance is updated when a cheaper route is found.")
    };

    private void loadQuizQuestion(){QuizQuestion q=questions[quizIndex];quizQuestionLabel.setText("Question "+(quizIndex+1)+" of "+questions.length+": "+q.question);for(int i=0;i<4;i++){quizOptions[i].setText(wrapRadioText(q.options[i],38));quizOptions[i].setSelected(false);quizOptions[i].setEnabled(true);}quizFeedbackLabel.setText(" ");quizCurrentAnswered=false;quizSubmitButton.setEnabled(true);quizNextButton.setEnabled(false);quizNextButton.setText(quizIndex==questions.length-1?"Restart Quiz":"Next Question");quizScoreLabel.setText("Score: "+quizScore+" / "+quizAnswered);}
    private void submitQuizAnswer(){if(quizCurrentAnswered)return;int selected=-1;for(int i=0;i<4;i++)if(quizOptions[i].isSelected())selected=i;if(selected<0){quizFeedbackLabel.setForeground(new Color(185,65,65));quizFeedbackLabel.setText("Select an answer before submitting.");return;}quizCurrentAnswered=true;quizAnswered++;QuizQuestion q=questions[quizIndex];if(selected==q.correct){quizScore++;quizFeedbackLabel.setForeground(new Color(22,128,74));quizFeedbackLabel.setText("Correct. "+q.explanation);}else{quizFeedbackLabel.setForeground(new Color(185,65,65));quizFeedbackLabel.setText("Incorrect. "+q.explanation);}for(int i=0;i<4;i++)quizOptions[i].setEnabled(false);quizSubmitButton.setEnabled(false);quizNextButton.setEnabled(true);quizScoreLabel.setText("Score: "+quizScore+" / "+quizAnswered);}
    private void nextQuizQuestion(){if(!quizCurrentAnswered)return;if(quizIndex==questions.length-1){quizIndex=0;quizScore=0;quizAnswered=0;}else quizIndex++;loadQuizQuestion();}

    // ---------------- Reference ----------------
    private JPanel createReferencePage(){
        JPanel page=pagePanel();page.setLayout(new BorderLayout(0,14));page.add(pageHeading("Quick reference","A compact revision sheet for key terms, formulas and algorithm steps."),BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(new Font("SansSerif",Font.PLAIN,12));
        tabs.addTab("Terminology",referenceText("KEY TERMS\n\nVertex (node)\nA point in a graph. In this project, a vertex represents a router.\n\nEdge (link)\nA connection between two vertices.\n\nWeight / cost\nA numerical value assigned to an edge.\n\nPath\nA sequence of vertices connected by edges.\n\nShortest path\nA path with the minimum sum of edge weights.\n\nSource\nThe starting vertex from which shortest paths are calculated.\n\nNext hop\nThe first router after the source on a selected route.\n\nRelaxation\nThe operation of checking whether a route through the current vertex improves a neighbor's tentative distance."));
        tabs.addTab("Formula & Complexity",referenceText("DISTANCE UPDATE\n\nIf dist[u] is the current shortest known distance to u and w(u,v) is the edge weight, calculate:\n\ncandidate = dist[u] + w(u,v)\n\nIf candidate < dist[v], update dist[v] = candidate and predecessor[v] = u.\n\nCOMPLEXITY IN THIS IMPLEMENTATION\n\nTime: O(V²)\nSpace: O(V²)\n\nV = number of vertices (routers). The adjacency matrix stores V × V entries. A linear scan selects the next unvisited vertex with the smallest tentative distance."));
        tabs.addTab("Pseudocode",referenceText("DIJKSTRA(source)\n\n1. Set every distance to infinity.\n2. Set the source distance to zero.\n3. Mark every vertex unvisited.\n4. Select the unvisited vertex u with minimum tentative distance.\n5. If no such reachable vertex exists, stop.\n6. Mark u as visited.\n7. For each unvisited neighbor v of u:\n      candidate = dist[u] + weight(u, v)\n      If candidate < dist[v]:\n          dist[v] = candidate\n          predecessor[v] = u\n8. Repeat until all reachable vertices are visited.\n9. Reconstruct routes using predecessor links."));
        page.add(tabs,BorderLayout.CENTER);return page;
    }

    private JScrollPane referenceText(String text){JTextArea area=new JTextArea(text);area.setEditable(false);area.setLineWrap(true);area.setWrapStyleWord(true);area.setFont(new Font("SansSerif",Font.PLAIN,14));area.setForeground(TEXT);area.setBackground(Color.WHITE);area.setBorder(new EmptyBorder(20,22,20,22));JScrollPane scroll=new JScrollPane(area);scroll.setBorder(new LineBorder(BORDER));return scroll;}

    // ---------------- Reusable UI ----------------
    private JPanel pagePanel(){JPanel p=new JPanel(new BorderLayout());p.setBackground(BG);p.setBorder(new EmptyBorder(24,26,24,26));return p;}
    private JPanel pageHeading(String title,String subtitle){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(wrappedDisplayText(title,1,new Font("SansSerif",Font.BOLD,24),NAVY));p.add(Box.createVerticalStrut(4));p.add(wrappedDisplayText(subtitle,1,new Font("SansSerif",Font.PLAIN,12),MUTED));return p;}
    private JPanel cardPanel(){JPanel p=new JPanel();p.setBackground(Color.WHITE);p.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true),new EmptyBorder(16,16,16,16)));return p;}
    private JPanel sectionTitle(String title,String subtitle){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(wrappedDisplayText(title,2,new Font("SansSerif",Font.BOLD,15),NAVY));p.add(Box.createVerticalStrut(4));p.add(wrappedDisplayText(subtitle,2,new Font("SansSerif",Font.PLAIN,12),MUTED));p.setAlignmentX(Component.LEFT_ALIGNMENT);return p;}
    private JTextArea wrappedDisplayText(String text,int rows,Font font,Color color){JTextArea a=new JTextArea(text,rows,20);a.setEditable(false);a.setFocusable(false);a.setLineWrap(true);a.setWrapStyleWord(true);a.setOpaque(false);a.setFont(font);a.setForeground(color);a.setBorder(null);a.setAlignmentX(Component.LEFT_ALIGNMENT);return a;}
    private String wrapRadioText(String text,int maxLineLength){StringBuilder html=new StringBuilder("<html>");int lineLength=0;for(String word:text.split(" ")){if(lineLength>0&&lineLength+1+word.length()>maxLineLength){html.append("<br>");lineLength=0;}else if(lineLength>0){html.append(' ');lineLength++;}html.append(word);lineLength+=word.length();}return html.append("</html>").toString();}
    private JTextArea wrappedText(String text,int rows){JTextArea a=new JTextArea(text,rows,16);a.setEditable(false);a.setLineWrap(true);a.setWrapStyleWord(true);a.setFont(new Font("SansSerif",Font.PLAIN,11));a.setForeground(MUTED);a.setBorder(null);return a;}
    private JButton primaryButton(String text){JButton b=new JButton(text);b.setFont(new Font("SansSerif",Font.BOLD,11));b.setForeground(Color.WHITE);b.setBackground(BLUE);b.setOpaque(true);b.setContentAreaFilled(true);b.setBorderPainted(false);b.setFocusPainted(false);b.setBorder(new EmptyBorder(9,14,9,14));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private JButton secondaryButton(String text){JButton b=new JButton(text);b.setFont(new Font("SansSerif",Font.BOLD,11));b.setForeground(NAVY);b.setBackground(new Color(248,250,252));b.setFocusPainted(false);b.setBorder(new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(8,12,8,12)));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private JLabel fieldLabel(String text){JLabel l=new JLabel(text);l.setFont(new Font("SansSerif",Font.BOLD,11));l.setForeground(MUTED);l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;}
    private JTable createTable(String[] columns){DefaultTableModel model=new DefaultTableModel(columns,0){public boolean isCellEditable(int r,int c){return false;}};JTable table=new JTable(model);table.setRowHeight(25);table.setFont(new Font("SansSerif",Font.PLAIN,11));table.setForeground(TEXT);table.setGridColor(BORDER);table.setSelectionBackground(LIGHT_BLUE);table.setSelectionForeground(NAVY);table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,10));table.getTableHeader().setBackground(new Color(248,250,252));table.getTableHeader().setForeground(NAVY);table.getTableHeader().setReorderingAllowed(false);table.setFillsViewportHeight(true);return table;}
    private void clearTable(JTable table){if(table!=null)((DefaultTableModel)table.getModel()).setRowCount(0);}
    private void error(String message){JOptionPane.showMessageDialog(this,message,"Input Error",JOptionPane.ERROR_MESSAGE);}

    // ---------------- Graph visualization ----------------
    private class NetworkPanel extends JPanel {
        private final Map<String,Point> positions=new HashMap<String,Point>();
        private ArrayList<ArrayList<String>> optimalPaths=new ArrayList<ArrayList<String>>();
        private String sourceRouter;
        NetworkPanel(){setBackground(new Color(250,251,253));setBorder(new LineBorder(BORDER));}
        void setOptimalPaths(ArrayList<ArrayList<String>> p){optimalPaths=p;}
        void setSourceRouter(String source){sourceRouter=source;}
        void clearPaths(){optimalPaths.clear();sourceRouter=null;}
        private boolean isOptimalEdge(String a,String b){for(List<String> p:optimalPaths)for(int i=0;i<p.size()-1;i++)if((p.get(i).equals(a)&&p.get(i+1).equals(b))||(p.get(i).equals(b)&&p.get(i+1).equals(a)))return true;return false;}
        protected void paintComponent(Graphics g){super.paintComponent(g);if(routers.isEmpty()){g.setColor(MUTED);g.setFont(new Font("SansSerif",Font.PLAIN,13));String m="Create a network to display its topology.";FontMetrics fm=g.getFontMetrics();g.drawString(m,Math.max(10,(getWidth()-fm.stringWidth(m))/2),getHeight()/2);return;}calculatePositions();Graphics2D g2=(Graphics2D)g.create();try{g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);drawEdges(g2);drawVertices(g2);drawLegend(g2);}finally{g2.dispose();}}
        private void calculatePositions(){positions.clear();int width=Math.max(getWidth(),1),height=Math.max(getHeight(),1);int centerX=width/2,centerY=height/2;int radius=Math.max(1,Math.min(width,height)/3);int count=routers.size();for(int i=0;i<count;i++){double angle=(2*Math.PI*i)/count-Math.PI/2;int x=centerX+(int)(radius*Math.cos(angle));int y=centerY+(int)(radius*Math.sin(angle));positions.put(routers.get(i),new Point(x,y));}}
        private void drawEdges(Graphics2D g){for(Edge e:edges){Point a=positions.get(e.from),b=positions.get(e.to);if(a==null||b==null)continue;boolean optimal=isOptimalEdge(e.from,e.to);g.setColor(optimal?BLUE:new Color(148,163,184));g.setStroke(new BasicStroke(optimal?4f:2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.draw(new Line2D.Double(a,b));String label=String.valueOf(e.cost);Font f=new Font("SansSerif",Font.BOLD,11);g.setFont(f);FontMetrics fm=g.getFontMetrics();int x=(a.x+b.x)/2,y=(a.y+b.y)/2;g.setColor(new Color(255,255,255,235));g.fillRoundRect(x-fm.stringWidth(label)/2-5,y-fm.getHeight()/2-2,fm.stringWidth(label)+10,fm.getHeight()+4,8,8);g.setColor(TEXT);g.drawString(label,x-fm.stringWidth(label)/2,y+fm.getAscent()/2-1);}}
        private void drawVertices(Graphics2D g){int d=42,r=d/2;for(String name:routers){Point p=positions.get(name);boolean source=name.equals(sourceRouter);g.setColor(source?BLUE:Color.WHITE);g.fillOval(p.x-r,p.y-r,d,d);g.setStroke(new BasicStroke(2));g.setColor(source?new Color(29,78,216):new Color(100,116,139));g.drawOval(p.x-r,p.y-r,d,d);g.setFont(new Font("SansSerif",Font.BOLD,15));FontMetrics fm=g.getFontMetrics();g.setColor(source?Color.WHITE:NAVY);g.drawString(name,p.x-fm.stringWidth(name)/2,p.y+(fm.getAscent()-fm.getDescent())/2);}}
        private void drawLegend(Graphics2D g){int y=getHeight()-13;g.setFont(new Font("SansSerif",Font.PLAIN,10));g.setColor(BLUE);g.fillOval(12,y-8,9,9);g.setColor(MUTED);g.drawString("Source",25,y);g.setColor(new Color(148,163,184));g.drawLine(82,y-4,103,y-4);g.setColor(MUTED);g.drawString("Other link",108,y);}
    }

    // ---------------- Data classes ----------------
    private static class Edge {final String from,to;final long cost;Edge(String f,String t,long c){from=f;to=t;cost=c;}}
    private static class Result {final String source;final long[] distance;final int[] previous;Result(String s,long[] d,int[] p){source=s;distance=d;previous=p;}}
    private static class QuizQuestion {final String question;final String[] options;final int correct;final String explanation;QuizQuestion(String q,String[] o,int c,String x){question=q;options=o;correct=c;explanation=x;}}

    public static void main(String[] args){SwingUtilities.invokeLater(new Runnable(){public void run(){try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}new DijkstraSimulator().setVisible(true);}});}
}
