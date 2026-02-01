/*
 SchoolManagementSystem.java
 A single-file Java Swing prototype of a School Management System.
 This is a runnable prototype (in-memory) demonstrating:
  - Dashboard overview
  - Student Information System (CRUD + profile)
  - Teacher management (CRUD)
  - Attendance marking
  - Gradebook (basic)
  - Fees / Payments (basic)

 Notes:
  - This is an in-memory demo using ArrayList storage. Replace with a DB for production.
  - Keep Java 11+ compatibility.
  - Compile: javac SchoolManagementSystem.java
  - Run:     java SchoolManagementSystem
*/

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;

public class SchoolManagementSystem {
    // In-memory storage
    static java.util.List<Student> students = new ArrayList<>();
    static java.util.List<Teacher> teachers = new ArrayList<>();
    static java.util.List<AttendanceRecord> attendance = new ArrayList<>();
    static java.util.List<GradeRecord> grades = new ArrayList<>();
    static java.util.List<Payment> payments = new ArrayList<>();

    // Main frame
    private JFrame frame;
    private JLabel lblStudentCount, lblTeacherCount, lblAttendanceCount, lblOutstandingFees;

    public static void main(String[] args) {
        // seed sample data
        seedDemoData();
        SwingUtilities.invokeLater(() -> new SchoolManagementSystem().createAndShowGUI());
    }

    static void seedDemoData() {
        students.add(new Student("S001", "Juan", "Dela Cruz", "09171234567", 16, "Grade 11", "Parent: Maria"));
        students.add(new Student("S002", "Ana", "Santos", "09179876543", 15, "Grade 10", "Parent: Pedro"));
        teachers.add(new Teacher("T001", "Ma. Clara", "Reyes", "Mathematics", "09170001111"));
        teachers.add(new Teacher("T002", "Jose", "Gonzalez", "Science", "09170002222"));
        payments.add(new Payment("S001", 5000.0, LocalDate.now().minusDays(10)));
    }

    private void createAndShowGUI() {
        frame = new JFrame("SCHOOL MANAGEMENT SYSTEM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);

        // Top: Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel title = new JLabel("SCHOOL MANAGEMENT SYSTEM", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        frame.add(header, BorderLayout.NORTH);

        // Center: tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", createDashboardPanel());
        tabs.addTab("Students", new StudentPanel());
        tabs.addTab("Teachers", new TeacherPanel());
        tabs.addTab("Attendance", new AttendancePanel());
        tabs.addTab("Grades", new GradesPanel());
        tabs.addTab("Fees", new FeesPanel());
        tabs.addTab("Reports", new ReportsPanel());

        frame.add(tabs, BorderLayout.CENTER);

        frame.setVisible(true);
        refreshDashboard();
    }

    private JPanel createDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 10));
        stats.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblStudentCount = createStatCard("Students", "0");
        lblTeacherCount = createStatCard("Teachers", "0");
        lblAttendanceCount = createStatCard("Attendance Records", "0");
        lblOutstandingFees = createStatCard("Payments", "0");

        stats.add(wrapCard(lblStudentCount));
        stats.add(wrapCard(lblTeacherCount));
        stats.add(wrapCard(lblAttendanceCount));
        stats.add(wrapCard(lblOutstandingFees));

        p.add(stats, BorderLayout.NORTH);

        JTextArea quick = new JTextArea();
        quick.setEditable(false);
        quick.setText("Quick Links:\n- Add Student / Teacher via tabs\n- Mark attendance\n- Enter grades\n- Record fee payments\n");
        quick.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(quick), BorderLayout.CENTER);

        return p;
    }

    private JLabel createStatCard(String title, String value) {
        JLabel l = new JLabel("<html><center>" + title + "<br/><span style='font-size:18px;font-weight:bold;'>" + value + "</span></center></html>");
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return l;
    }

    private JPanel wrapCard(JLabel label) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private void refreshDashboard() {
        lblStudentCount.setText("<html><center>Students<br/><span style='font-size:18px;font-weight:bold;'>" + students.size() + "</span></center></html>");
        lblTeacherCount.setText("<html><center>Teachers<br/><span style='font-size:18px;font-weight:bold;'>" + teachers.size() + "</span></center></html>");
        lblAttendanceCount.setText("<html><center>Attendance Records<br/><span style='font-size:18px;font-weight:bold;'>" + attendance.size() + "</span></center></html>");
        lblOutstandingFees.setText("<html><center>Payments<br/><span style='font-size:18px;font-weight:bold;'>" + payments.size() + "</span></center></html>");
    }

    // ----------------------- Panels -----------------------
    class StudentPanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        StudentPanel() {
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new Object[]{"ID", "First Name", "Last Name", "Phone", "Age", "Level", "Guardian"}, 0) {
                public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(model);
            refreshTable();
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel controls = new JPanel();
            JButton btnAdd = new JButton("Add Student");
            JButton btnEdit = new JButton("Edit Student");
            JButton btnDelete = new JButton("Delete Student");
            JButton btnView = new JButton("View Profile");

            btnAdd.addActionListener(e -> openStudentDialog(null));
            btnEdit.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r == -1) { showMsg("Select a student first."); return; }
                String id = (String)model.getValueAt(r, 0);
                openStudentDialog(findStudentById(id));
            });
            btnDelete.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r == -1) { showMsg("Select a student first."); return; }
                String id = (String)model.getValueAt(r, 0);
                int ok = JOptionPane.showConfirmDialog(this, "Delete student " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                    students.removeIf(s -> s.id.equals(id));
                    refreshTable(); refreshDashboard();
                }
            });
            btnView.addActionListener(e -> {
                int r = table.getSelectedRow(); if (r == -1) { showMsg("Select a student first."); return; }
                String id = (String)model.getValueAt(r, 0);
                Student s = findStudentById(id);
                showProfileDialog(s);
            });

            controls.add(btnAdd); controls.add(btnEdit); controls.add(btnDelete); controls.add(btnView);
            add(controls, BorderLayout.SOUTH);
        }

        void refreshTable() {
            model.setRowCount(0);
            for (Student s : students) model.addRow(new Object[]{s.id, s.firstName, s.lastName, s.phone, s.age, s.level, s.guardian});
        }

        Student findStudentById(String id) { return students.stream().filter(s -> s.id.equals(id)).findFirst().orElse(null); }

        void openStudentDialog(Student s) {
            boolean isNew = (s == null);
            JTextField tfId = new JTextField(isNew ? generateStudentId() : s.id);
            JTextField tfFirst = new JTextField(isNew ? "" : s.firstName);
            JTextField tfLast = new JTextField(isNew ? "" : s.lastName);
            JTextField tfPhone = new JTextField(isNew ? "" : s.phone);
            JTextField tfAge = new JTextField(isNew ? "" : String.valueOf(s.age));
            JTextField tfLevel = new JTextField(isNew ? "" : s.level);
            JTextField tfGuardian = new JTextField(isNew ? "" : s.guardian);

            JPanel p = new JPanel(new GridLayout(0,2,6,6));
            p.add(new JLabel("ID")); p.add(tfId);
            p.add(new JLabel("First Name")); p.add(tfFirst);
            p.add(new JLabel("Last Name")); p.add(tfLast);
            p.add(new JLabel("Phone")); p.add(tfPhone);
            p.add(new JLabel("Age")); p.add(tfAge);
            p.add(new JLabel("Level")); p.add(tfLevel);
            p.add(new JLabel("Guardian")); p.add(tfGuardian);

            int result = JOptionPane.showConfirmDialog(this, p, isNew?"Add Student":"Edit Student", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    if (isNew) {
                        Student ns = new Student(tfId.getText().trim(), tfFirst.getText().trim(), tfLast.getText().trim(), tfPhone.getText().trim(), Integer.parseInt(tfAge.getText().trim()), tfLevel.getText().trim(), tfGuardian.getText().trim());
                        students.add(ns);
                    } else {
                        s.firstName = tfFirst.getText().trim();
                        s.lastName = tfLast.getText().trim();
                        s.phone = tfPhone.getText().trim();
                        s.age = Integer.parseInt(tfAge.getText().trim());
                        s.level = tfLevel.getText().trim();
                        s.guardian = tfGuardian.getText().trim();
                    }
                    refreshTable(); refreshDashboard();
                } catch (Exception ex) { showMsg("Error saving: " + ex.getMessage()); }
            }
        }

        void showProfileDialog(Student s) {
            if (s == null) { showMsg("Student not found."); return; }
            JTextArea ta = new JTextArea(); ta.setEditable(false);
            ta.setText("ID: " + s.id + "\nName: " + s.firstName + " " + s.lastName + "\nPhone: " + s.phone + "\nAge: " + s.age + "\nLevel: " + s.level + "\nGuardian: " + s.guardian);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Profile: " + s.id, JOptionPane.INFORMATION_MESSAGE);
        }

        String generateStudentId() {
            return String.format("S%03d", students.size() + 1 + new Random().nextInt(900));
        }

        void showMsg(String msg) { JOptionPane.showMessageDialog(this, msg); }
    }

    class TeacherPanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        TeacherPanel() {
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new Object[]{"ID","First","Last","Subject","Phone"}, 0) {
                public boolean isCellEditable(int row, int col) { return false; }
            };
            table = new JTable(model);
            refreshTable();
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel c = new JPanel();
            JButton add = new JButton("Add");
            JButton edit = new JButton("Edit");
            JButton del = new JButton("Delete");
            add.addActionListener(e -> openDialog(null));
            edit.addActionListener(e -> {
                int r = table.getSelectedRow(); if (r==-1) { showMsg("Select a teacher."); return; }
                String id = (String)model.getValueAt(r,0); openDialog(findById(id));
            });
            del.addActionListener(e -> {
                int r = table.getSelectedRow(); if (r==-1) { showMsg("Select a teacher."); return; }
                String id = (String)model.getValueAt(r,0);
                teachers.removeIf(t->t.id.equals(id)); refreshTable(); refreshDashboard();
            });
            c.add(add); c.add(edit); c.add(del);
            add(c, BorderLayout.SOUTH);
        }

        void refreshTable() {
            model.setRowCount(0);
            for (Teacher t : teachers) model.addRow(new Object[]{t.id, t.firstName, t.lastName, t.subject, t.phone});
        }

        Teacher findById(String id) { return teachers.stream().filter(t->t.id.equals(id)).findFirst().orElse(null); }

        void openDialog(Teacher t) {
            boolean isNew = (t==null);
            JTextField tfId = new JTextField(isNew?generateTeacherId():t.id);
            JTextField tfFirst = new JTextField(isNew?"":t.firstName);
            JTextField tfLast = new JTextField(isNew?"":t.lastName);
            JTextField tfSub = new JTextField(isNew?"":t.subject);
            JTextField tfPhone = new JTextField(isNew?"":t.phone);
            JPanel p = new JPanel(new GridLayout(0,2,6,6));
            p.add(new JLabel("ID")); p.add(tfId);
            p.add(new JLabel("First")); p.add(tfFirst);
            p.add(new JLabel("Last")); p.add(tfLast);
            p.add(new JLabel("Subject")); p.add(tfSub);
            p.add(new JLabel("Phone")); p.add(tfPhone);
            int res = JOptionPane.showConfirmDialog(this, p, isNew?"Add Teacher":"Edit Teacher", JOptionPane.OK_CANCEL_OPTION);
            if (res==JOptionPane.OK_OPTION) {
                if (isNew) {
                    teachers.add(new Teacher(tfId.getText().trim(), tfFirst.getText().trim(), tfLast.getText().trim(), tfSub.getText().trim(), tfPhone.getText().trim()));
                } else {
                    t.firstName = tfFirst.getText().trim(); t.lastName = tfLast.getText().trim(); t.subject = tfSub.getText().trim(); t.phone = tfPhone.getText().trim();
                }
                refreshTable(); refreshDashboard();
            }
        }

        String generateTeacherId() { return String.format("T%03d", teachers.size()+1 + new Random().nextInt(300)); }
        void showMsg(String m) { JOptionPane.showMessageDialog(this, m); }
    }

    class AttendancePanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        AttendancePanel() {
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new Object[]{"Student ID","Name","Date","Status"}, 0) {
                public boolean isCellEditable(int r,int c){return false;}
            };
            table = new JTable(model);
            refreshTable();
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel c = new JPanel();
            JButton mark = new JButton("Mark Attendance");
            JButton view = new JButton("View By Date");
            mark.addActionListener(e -> markAttendance());
            view.addActionListener(e -> refreshTable());
            c.add(mark); c.add(view);
            add(c, BorderLayout.SOUTH);
        }

        void markAttendance() {
            // choose student
            String[] ids = students.stream().map(s->s.id + " - " + s.firstName + " " + s.lastName).toArray(String[]::new);
            if (ids.length==0) { JOptionPane.showMessageDialog(this, "No students available."); return; }
            String sel = (String)JOptionPane.showInputDialog(this, "Select student:", "Mark", JOptionPane.PLAIN_MESSAGE, null, ids, ids[0]);
            if (sel==null) return;
            String sid = sel.split(" - ")[0];
            String[] opts = new String[]{"Present","Absent","Late","Excused"};
            String status = (String)JOptionPane.showInputDialog(this, "Status:", "Mark", JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            if (status==null) return;
            attendance.add(new AttendanceRecord(sid, LocalDate.now(), status));
            refreshTable(); refreshDashboard();
        }

        void refreshTable() {
            model.setRowCount(0);
            for (AttendanceRecord a : attendance) {
                Student s = students.stream().filter(x->x.id.equals(a.studentId)).findFirst().orElse(null);
                String name = s==null?"(deleted)":(s.firstName + " " + s.lastName);
                model.addRow(new Object[]{a.studentId, name, a.date.toString(), a.status});
            }
        }
    }

    class GradesPanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        GradesPanel() {
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new Object[]{"Student ID","Name","Subject","Grade"},0){ public boolean isCellEditable(int r,int c){return false;} };
            table = new JTable(model);
            refreshTable();
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel c = new JPanel();
            JButton add = new JButton("Enter Grade");
            JButton calc = new JButton("Auto Average");
            add.addActionListener(e -> enterGrade());
            calc.addActionListener(e -> JOptionPane.showMessageDialog(this, "Average calculation is demo-only."));
            c.add(add); c.add(calc);
            add(c, BorderLayout.SOUTH);
        }

        void enterGrade() {
            String[] ids = students.stream().map(s->s.id + " - " + s.firstName + " " + s.lastName).toArray(String[]::new);
            if (ids.length==0) { JOptionPane.showMessageDialog(this, "No students."); return; }
            String sel = (String)JOptionPane.showInputDialog(this, "Student:", "Grade", JOptionPane.PLAIN_MESSAGE, null, ids, ids[0]);
            if (sel==null) return;
            String sid = sel.split(" - ")[0];
            String subj = JOptionPane.showInputDialog(this, "Subject:"); if (subj==null) return;
            String gstr = JOptionPane.showInputDialog(this, "Grade (numeric):"); if (gstr==null) return;
            try {
                double g = Double.parseDouble(gstr);
                grades.add(new GradeRecord(sid, subj, g));
                refreshTable();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid grade."); }
        }

        void refreshTable() {
            model.setRowCount(0);
            for (GradeRecord gr : grades) {
                Student s = students.stream().filter(x->x.id.equals(gr.studentId)).findFirst().orElse(null);
                String name = s==null?"(deleted)":(s.firstName + " " + s.lastName);
                model.addRow(new Object[]{gr.studentId, name, gr.subject, gr.grade});
            }
        }
    }

    class FeesPanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        FeesPanel() {
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new Object[]{"Student ID","Name","Amount","Date"},0) { public boolean isCellEditable(int r,int c){return false;} };
            table = new JTable(model);
            refreshTable();
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel c = new JPanel();
            JButton pay = new JButton("Record Payment");
            JButton report = new JButton("View Outstanding");
            pay.addActionListener(e -> recordPayment());
            report.addActionListener(e -> viewOutstanding());
            c.add(pay); c.add(report);
            add(c, BorderLayout.SOUTH);
        }

        void recordPayment() {
            String[] ids = students.stream().map(s->s.id + " - " + s.firstName + " " + s.lastName).toArray(String[]::new);
            if (ids.length==0) { JOptionPane.showMessageDialog(this, "No students."); return; }
            String sel = (String)JOptionPane.showInputDialog(this, "Student:", "Payment", JOptionPane.PLAIN_MESSAGE, null, ids, ids[0]);
            if (sel==null) return;
            String sid = sel.split(" - ")[0];
            String amt = JOptionPane.showInputDialog(this, "Amount:"); if (amt==null) return;
            try {
                double a = Double.parseDouble(amt);
                payments.add(new Payment(sid, a, LocalDate.now()));
                refreshTable(); refreshDashboard();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid amount."); }
        }

        void viewOutstanding() {
            JOptionPane.showMessageDialog(this, "Outstanding feature requires fee structure to compute. This demo stores payments only.");
        }

        void refreshTable() {
            model.setRowCount(0);
            for (Payment p : payments) {
                Student s = students.stream().filter(x->x.id.equals(p.studentId)).findFirst().orElse(null);
                String name = s==null?"(deleted)":(s.firstName + " " + s.lastName);
                model.addRow(new Object[]{p.studentId, name, p.amount, p.date.toString()});
            }
        }
    }

    class ReportsPanel extends JPanel {
        ReportsPanel() {
            setLayout(new BorderLayout());
            JTextArea ta = new JTextArea(); ta.setEditable(false);
            ta.setText("Reports (demo):\n- Attendance by date\n- Student lists\n- Payments summary\n\nUse export functionality in production to generate PDF/CSV.");
            add(new JScrollPane(ta), BorderLayout.CENTER);
        }
    }

    // ----------------------- Models -----------------------
    static class Student {
        String id, firstName, lastName, phone, level, guardian;
        int age;
        public Student(String id, String firstName, String lastName, String phone, int age, String level, String guardian) {
            this.id = id; this.firstName = firstName; this.lastName = lastName; this.phone = phone; this.age = age; this.level = level; this.guardian = guardian;
        }
    }
    static class Teacher { String id, firstName, lastName, subject, phone; public Teacher(String id, String f, String l, String sub, String ph){this.id=id;this.firstName=f;this.lastName=l;this.subject=sub;this.phone=ph;} }
    static class AttendanceRecord { String studentId; LocalDate date; String status; public AttendanceRecord(String sid, LocalDate d, String st){this.studentId=sid;this.date=d;this.status=st;} }
    static class GradeRecord { String studentId, subject; double grade; public GradeRecord(String sid, String subject, double g){this.studentId=sid;this.subject=subject;this.grade=g;} }
    static class Payment { String studentId; double amount; LocalDate date; public Payment(String sid, double amt, LocalDate d){this.studentId=sid;this.amount=amt;this.date=d;} }
}
