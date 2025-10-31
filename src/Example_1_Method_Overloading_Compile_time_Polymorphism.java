public class Example_1_Method_Overloading_Compile_time_Polymorphism {
    public static class MathOperation {
        public int add(int a, int b) {
            return a + b;
        }
        public double add(double a, double b) {
            return a + b;
        }

    }
    public static void main(String[] args) {
        MathOperation m = new MathOperation();
        System.out.println(m.add(10, 20));
        System.out.println(m.add(5.5,5.5));
    }
}
