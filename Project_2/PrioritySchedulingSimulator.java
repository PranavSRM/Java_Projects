import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PrioritySchedulingSimulator extends JFrame {
    private DefaultTableModel processTableModel;
    private DefaultTableModel finishedTableModel;
    private JTable processTable;
    private JTable finishedTable;
    private GanttPanel ganttPanel;

    private java.util.Timer simTimer;
    private int currentTime = 0;
    private boolean running = false;
    private int pidCounter = 1;
    private java.util.List<Proc> processes = new ArrayList<>();
    private java.util.List<GanttEntry> gantt = new ArrayList<>();

    private JLabel lblTime;
    private JLabel lblStats;
    private JSlider speedSlider;

    public PrioritySchedulingSimulator() {
        super("Priority Scheduling Simulator (Preemptive)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));

        JPanel left = new JPanel();
        left.setLayout(new BorderLayout(8,8));
        left.setPreferredSize(new Dimension(360, getHeight()));

        JPanel input = new JPanel(new GridBagLayout());
        input.setBorder(new TitledBorder("Add Process"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx = 0; c.gridy = 0; input.add(new JLabel("Arrival Time:"), c);
        JTextField tfArrival = new JTextField("0",8);
        c.gridx=1; input.add(tfArrival,c);
        c.gridx=0; c.gridy=1; input.add(new JLabel("Burst Time:"), c);
        JTextField tfBurst = new JTextField("5",8);
        c.gridx=1; input.add(tfBurst,c);
        c.gridx=0; c.gridy=2; input.add(new JLabel("Priority (higher=more):"), c);
        JTextField tfPriority = new JTextField("1",8);
        c.gridx=1; input.add(tfPriority,c);
        c.gridx=0; c.gridy=3; c.gridwidth=2;
        JButton btnAdd = new JButton("Add Process");
        input.add(btnAdd,c);

        processTableModel = new DefaultTableModel(new Object[]{"PID","Arrival","Burst","Remaining","Priority"},0) {
            public boolean isCellEditable(int row,int col){return false;}
        };
        processTable = new JTable(processTableModel);
        JScrollPane spTable = new JScrollPane(processTable);
        spTable.setBorder(new TitledBorder("Process List"));
        spTable.setPreferredSize(new Dimension(340,200));

        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnStart = new JButton("Start");
        JButton btnPause = new JButton("Pause");
        JButton btnStep = new JButton("Step");
        JButton btnReset = new JButton("Reset");
        control.add(btnStart); control.add(btnPause); control.add(btnStep); control.add(btnReset);

        lblTime = new JLabel("Time: 0");
        speedSlider = new JSlider(100, 1000, 400);
        speedSlider.setMajorTickSpacing(300);
        speedSlider.setMinorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setBorder(new TitledBorder("Simulation Speed (ms / tick)"));

        lblStats = new JLabel("Avg Waiting: - , Avg Turnaround: -");

        left.add(input, BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(6,6));
        mid.add(spTable, BorderLayout.CENTER);
        mid.add(control, BorderLayout.SOUTH);
        left.add(mid, BorderLayout.CENTER);
        JPanel bottomLeft = new JPanel(new GridLayout(3,1));
        bottomLeft.add(lblTime); bottomLeft.add(speedSlider); bottomLeft.add(lblStats);
        left.add(bottomLeft, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(8,8));

        ganttPanel = new GanttPanel();
        ganttPanel.setPreferredSize(new Dimension(600,250));
        ganttPanel.setBorder(new TitledBorder("Gantt Chart"));

        finishedTableModel = new DefaultTableModel(new Object[]{"PID","Arrival","Burst","Priority","Start","Completion","Waiting","Turnaround"},0) {
            public boolean isCellEditable(int row,int col){return false;}
        };
        finishedTable = new JTable(finishedTableModel);
        JScrollPane spFinished = new JScrollPane(finishedTable);
        spFinished.setBorder(new TitledBorder("Finished Processes"));

        right.add(ganttPanel, BorderLayout.NORTH);
        right.add(spFinished, BorderLayout.CENTER);

        add(right, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            try {
                int arrival = Integer.parseInt(tfArrival.getText().trim());
                int burst = Integer.parseInt(tfBurst.getText().trim());
                int pr = Integer.parseInt(tfPriority.getText().trim());
                if (burst <= 0) throw new NumberFormatException();
                addProcess(arrival, burst, pr);
                tfBurst.setText("5"); tfArrival.setText("0"); tfPriority.setText("1");
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this, "Please enter valid integers (Burst > 0)", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnStart.addActionListener(e -> {
            if (!running) startSimulation();
        });
        btnPause.addActionListener(e -> pauseSimulation());
        btnReset.addActionListener(e -> resetSimulation());
        btnStep.addActionListener(e -> stepSimulation());

        processTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if (SwingUtilities.isRightMouseButton(e)){
                    int r = processTable.rowAtPoint(e.getPoint());
                    if (r >= 0){
                        int pid = (int) processTableModel.getValueAt(r,0);
                        removeProcess(pid);
                    }
                }
            }
        });
    }

    private void addProcess(int arrival, int burst, int priority){
        Proc p = new Proc(pidCounter++, arrival, burst, priority);
        processes.add(p);
        processTableModel.addRow(new Object[]{p.pid, p.arrivalTime, p.burstTime, p.remainingTime, p.priority});
        repaint();
    }

    private void removeProcess(int pid){
        processes.removeIf(p->p.pid==pid);
        for (int i = processTableModel.getRowCount()-1;i>=0;i--){
            if ((int)processTableModel.getValueAt(i,0)==pid) processTableModel.removeRow(i);
        }
    }

    private void startSimulation(){
        running = true;
        if (simTimer != null) simTimer.cancel();
        simTimer = new java.util.Timer();
        scheduleTimerTick();
    }

    private void scheduleTimerTick(){
        int delay = speedSlider.getValue();
        simTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    stepSimulation();
                });
            }
        }, 0, delay);
    }

    private void pauseSimulation(){
        running = false;
        if (simTimer != null) {
            simTimer.cancel(); simTimer = null;
        }
    }

    private void resetSimulation(){
        pauseSimulation();
        currentTime = 0;
        gid = 0;
        gantt.clear();
        processes.clear();
        processTableModel.setRowCount(0);
        finishedTableModel.setRowCount(0);
        pidCounter = 1;
        lblTime.setText("Time: 0");
        lblStats.setText("Avg Waiting: - , Avg Turnaround: -");
        ganttPanel.repaint();
    }

    private int gid = 0;

    private void stepSimulation(){

        boolean anyRemaining = processes.stream().anyMatch(p->p.remainingTime>0) || processes.stream().anyMatch(p->p.arrivalTime>currentTime);

        boolean anyToRun = processes.stream().anyMatch(p->p.remainingTime>0 && p.arrivalTime<=currentTime);
        if (!anyToRun){

            OptionalInt nxt = processes.stream().filter(p->p.remainingTime>0 && p.arrivalTime>currentTime).mapToInt(p->p.arrivalTime).min();
            if (nxt.isPresent()){

                currentTime = nxt.getAsInt();
                lblTime.setText("Time: " + currentTime);
                return;
            } else {
                pauseSimulation();
                computeStats();
                return;
            }
        }
        Proc pick = processes.stream()
                .filter(p->p.remainingTime>0 && p.arrivalTime<=currentTime)
                .sorted(Comparator.comparingInt(Proc::getPriority).reversed() 
                        .thenComparingInt(Proc::getArrivalTime)
                        .thenComparingInt(Proc::getPid))
                .findFirst().orElse(null);

        if (pick == null){
            currentTime++;
            lblTime.setText("Time: " + currentTime);
            return;
        }

        if (pick.startTime == -1) pick.startTime = currentTime;

        pick.remainingTime -= 1;

        if (gantt.isEmpty() || gantt.get(gantt.size()-1).pid != pick.pid){
            gantt.add(new GanttEntry(pick.pid, currentTime, currentTime+1, gid++));
        } else {
            GanttEntry last = gantt.get(gantt.size()-1);
            last.end = currentTime+1;
        }

        currentTime++;
        lblTime.setText("Time: " + currentTime);

        for (int i = 0;i<processTableModel.getRowCount();i++){
            int pid = (int) processTableModel.getValueAt(i,0);
            for (Proc p: processes) if (p.pid==pid){
                processTableModel.setValueAt(p.remainingTime,i,3);
            }
        }

        if (pick.remainingTime==0){
            pick.completionTime = currentTime;
            int waiting = pick.completionTime - pick.arrivalTime - pick.burstTime;
            int tat = pick.completionTime - pick.arrivalTime;
            finishedTableModel.addRow(new Object[]{pick.pid, pick.arrivalTime, pick.burstTime, pick.priority, pick.startTime, pick.completionTime, waiting, tat});
        }

        ganttPanel.setGanttData(gantt, processes);
        ganttPanel.repaint();

        boolean anyLeft = processes.stream().anyMatch(p->p.remainingTime>0);
        if (!anyLeft){
            pauseSimulation();
            computeStats();
        }
    }

    private void computeStats(){
        int n = 0; double totalWaiting=0, totalTAT=0;
        for (Proc p: processes){
            if (p.completionTime>=0){
                int wt = p.completionTime - p.arrivalTime - p.burstTime;
                int tat = p.completionTime - p.arrivalTime;
                totalWaiting += wt; totalTAT += tat; n++;
            }
        }
        if (n>0) lblStats.setText(String.format("Avg Waiting: %.2f , Avg Turnaround: %.2f", totalWaiting/n, totalTAT/n));
    }

    // --- Inner classes ---
    static class Proc{
        int pid;
        int arrivalTime;
        int burstTime;
        int remainingTime;
        int priority;
        int startTime = -1;
        int completionTime = -1;

        Proc(int pid,int a,int b,int pr){
            this.pid=pid; this.arrivalTime=a; this.burstTime=b; this.remainingTime=b; this.priority=pr;
        }

        public int getPriority(){return priority;}
        public int getArrivalTime(){return arrivalTime;}
        public int getPid(){return pid;}
    }

    static class GanttEntry{
        int pid; int start; int end; int gid;
        GanttEntry(int pid,int s,int e,int gid){this.pid=pid; this.start=s; this.end=e; this.gid=gid;}
    }

    class GanttPanel extends JPanel{
        private java.util.List<GanttEntry> data = new ArrayList<>();
        private Map<Integer,Color> colorMap = new HashMap<>();

        public void setGanttData(java.util.List<GanttEntry> d, java.util.List<Proc> procs){
            data = new ArrayList<>(d);
            for (Proc p: procs){
                if (!colorMap.containsKey(p.pid)) colorMap.put(p.pid, new Color(Math.abs(p.pid*37) % 200 + 20, Math.abs(p.pid*53)%200+20, Math.abs(p.pid*97)%200+20));
            }
        }

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int padding=40;
            int w = getWidth()-padding*2;
            int h = getHeight()-padding*2;

            int maxTime = Math.max(currentTime, data.stream().mapToInt(d->d.end).max().orElse(0));
            if (maxTime==0) return;
            double unitW = (double) w / Math.max(1, maxTime);
            g2.setColor(Color.LIGHT_GRAY);
            for (int t=0;t<=maxTime;t++){
                int x = padding + (int) Math.round(t*unitW);
                g2.drawLine(x, padding, x, padding+h);
            }
            for (GanttEntry e: data){
                int x = padding + (int) Math.round(e.start * unitW);
                int blockW = Math.max(2, (int) Math.round((e.end - e.start) * unitW));
                Color c = colorMap.getOrDefault(e.pid, Color.GRAY);
                g2.setColor(c);
                g2.fillRoundRect(x, padding + 10, blockW, 40, 8,8);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(x, padding +10, blockW, 40,8,8);
                g2.drawString("P"+e.pid, x+4, padding + 35);
            }
            g2.setColor(Color.BLACK);
            for (int t=0;t<=maxTime;t++){
                int x = padding + (int) Math.round(t*unitW);
                g2.drawString(String.valueOf(t), x-3, padding + 65 + 20);
            }
            int lx = padding + w - 150; int ly = padding;
            g2.drawString("Ready / Legend:", lx, ly-6);
            int ry = ly;
            java.util.List<Proc> ready = new ArrayList<>();
            for (Proc p: processes) if (p.remainingTime>0 && p.arrivalTime<=currentTime) ready.add(p);
            ready.sort(Comparator.comparingInt(Proc::getPriority).reversed().thenComparingInt(Proc::getArrivalTime));
            for (Proc p: ready){
                Color c = colorMap.getOrDefault(p.pid, Color.GRAY);
                g2.setColor(c);
                g2.fillRect(lx, ry+4, 12,12);
                g2.setColor(Color.BLACK);
                g2.drawString("P"+p.pid+"(rem="+p.remainingTime+",pr="+p.priority+")", lx+18, ry+15);
                ry += 18;
            }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            PrioritySchedulingSimulator app = new PrioritySchedulingSimulator();
            app.setVisible(true);
        });
    }
}
