package io.github.euonmyoji.pooltouhouinterface.util;

/**
 * @author yinyangshi
 */
public class CalcStack {
    private final double[] stack;
    private int top = -1;

    public CalcStack() {
        stack = new double[64];
    }


    public void push(double v) {
        stack[++top] = v;
    }

    public double pop() {
        return stack[top--];
    }

    public void add() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] + v;
    }

    public void sub() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] - v;
    }

    public void mul() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] * v;
    }

    public void div() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] / v;
    }

    public void mod() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] % v;
    }

    public void eq() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] == v ? 1.0 : 0.0;
    }

    public void nq() {
        double v = stack[top];
        top -= 1;
        //noinspection ConditionalExpressionWithNegatedCondition
        stack[top] = stack[top] != v ? 1.0 : 0.0;
    }

    public void lt() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] < v ? 1.0 : 0.0;
    }

    public void gt() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] > v ? 1.0 : 0.0;
    }

    public void le() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] <= v ? 1.0 : 0.0;

    }

    public void ge() {
        double v = stack[top];
        top -= 1;
        stack[top] = stack[top] >= v ? 1.0 : 0.0;
    }

}
