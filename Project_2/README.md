Priority Scheduling Simulator (Java GUI)

A Java Swing-based GUI application that visually demonstrates Priority Scheduling (Preemptive) in Operating Systems.

🧩 Features

Add new processes with custom Arrival Time, Burst Time, and Priority.

Automatic PID assignment for each process.

Preemptive Priority Scheduling algorithm (higher numeric value = higher priority).

Interactive controls:

▶️ Start – Begin or resume the simulation.

⏸️ Pause – Temporarily stop the simulation.

⏩ Step – Move forward by one time unit manually.

🔄 Reset – Clear all data and restart.

Adjustable simulation speed using a slider (milliseconds per time tick).

Real-time Gantt Chart visualization with color-coded process blocks.

Process List Table showing current states and remaining burst times.

Finished Processes Table displaying:

Start Time

Completion Time

Waiting Time

Turnaround Time

Performance metrics (average waiting and turnaround time).

🧠 Algorithm Overview

The Priority Scheduling (Preemptive) algorithm selects the process with the highest priority value at each time unit. If two processes share the same priority, the scheduler chooses based on earliest arrival time and then lowest PID.

Steps:

Check all processes that have arrived.

Select the process with the highest priority.

Execute it for one time unit.

If a higher priority process arrives, preempt the current one.

Repeat until all processes finish.
