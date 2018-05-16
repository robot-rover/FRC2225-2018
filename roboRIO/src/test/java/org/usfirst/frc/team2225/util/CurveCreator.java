package org.usfirst.frc.team2225.util;

import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.BiFunction;

public class CurveCreator {
    public static void main (String[] args) {
        new CurveViewer(CurveCreator::teleopJoystickProcessor, 3).setVisible(true);
    }

    public static double teleopJoystickProcessor(double valIn, double param[]) {
        /*double val = Drivetrain.deadzone(param[0], valIn);
        if(val > 0)
            val = Drivetrain.padMinValue(param[1], val, true);
        val = Math.copySign(Math.pow(val, param[2] * 3), val);*/
        double val = valIn;
        val = Drivetrain.deadzone(param[0], val);
        val = Math.copySign(Math.pow(Math.abs(val), param[2] * 3), val);

        if(Math.abs(val) > 0)
            val = Drivetrain.padMinValue(param[1], val, true);
        return val;
    }
}
class CurveViewer extends JFrame {
    JPanel jp;
    BiFunction<Double, double[], Double> curveFunction;
    double input;
    private JSlider[] sliders;
    private JLabel[] labels;
    private double[] params;
    private JSlider inputSlider;

    public CurveViewer(BiFunction<Double, double[], Double> curveFunction, int paramLength) {
        super("Curve Viewer");

        sliders = new JSlider[paramLength];
        labels = new JLabel[paramLength];
        params = new double[paramLength];
        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.curveFunction = curveFunction;
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        jp = new GPanel();
        getContentPane().add("Graph", jp);
        inputSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 300, 0);
        inputSlider.setSnapToTicks(false);

        JLabel inputLabel = new JLabel();
        inputSlider.addChangeListener(e -> {
            input = inputSlider.getValue() / 300.0;
            inputLabel.setText("Input Value: " + input + " | Output Value: " + String.format("%.2f",curveFunction.apply(input, params)));
            jp.repaint();
        });
        getContentPane().add(inputLabel);
        getContentPane().add(inputSlider);
        for(int i = 0; i < paramLength; i++) {
            labels[i] = new JLabel("Input Value: ");
            getContentPane().add(labels[i]);
            sliders[i] = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
            final int j = i;
            sliders[i].addChangeListener(new Listener(i));
            sliders[i].setSnapToTicks(false);
            getContentPane().add(sliders[i]);
        }
        pack();
    }

    class Listener implements ChangeListener {
        JLabel label;
        JSlider slider;
        int index;
        Listener(int index) {
            this.label = labels[index];
            this.slider = sliders[index];
            this.index = index;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            label.setText("Parameter Value: " + slider.getValue() / 100.0);
            params[index] = slider.getValue() / 100.0;
            jp.repaint();
        }
    }

    class GPanel extends JPanel {
        public GPanel() {
            super.setPreferredSize(new Dimension(520, 530));
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println(e.getX());
                    inputSlider.setValue((e.getX()-10)/5*3);
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            //rectangle originated at 10,10 and end at 240,240
            g.setColor(new Color(150, 150, 150));
            g.drawRect(10, 10, 500, 500);
            g.setColor(Color.BLACK);
            //range for x 0-500 (function input range = 0-1 and output range 0-1)
            int x1, x2;
            double y1, y2;
            x1 = 0;
            y1 = curveFunction.apply(x1/500.0, params);
            while(x1 < 500) {
                x2 = x1 + 1;
                y2 = curveFunction.apply(x1/500.0, params);
                g.drawLine(x1 + 10, 510 - (int)(y1 * 500), x2 + 10, 510 - (int)(y2 * 500));
                x1 = x2;
                y1 = y2;
            }
            g.setColor(Color.red);
            g.drawLine((int)(input*500) + 10, 510, (int)(input*500) + 10, 510 - (int)(curveFunction.apply(input, params) * 500));
            g.drawOval((int)(input*500) + 8, 508 - (int)(curveFunction.apply(input, params) * 500), 4, 4);
        }


    }
}
