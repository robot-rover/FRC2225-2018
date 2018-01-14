package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import org.jfree.chart.*;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain.*;

public class OmniDriveVisualize extends ApplicationFrame {
    XYSeriesCollection dataset;
    ChartPanel panel;

    /**
     * Constructs a new application frame.
     *
     * @param title the frame title.
     */
    public OmniDriveVisualize(String title) {
        super(title);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dataset = new XYSeriesCollection();
        //drawPlot(51);
        JFreeChart chart = ChartFactory.createXYLineChart("OmniDriveVectorTest", "X", "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
        panel = new ChartPanel(chart);
        panel.setMouseZoomable(false);
        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setAutoRange(false);
        plot.getRangeAxis().setLowerBound(-2);
        plot.getRangeAxis().setUpperBound(2);
        plot.getDomainAxis().setAutoRange(false);
        plot.getDomainAxis().setLowerBound(-2);
        plot.getDomainAxis().setUpperBound(2);
        panel.setPreferredSize(new java.awt.Dimension(800, 800));
        panel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                dataset.removeAllSeries();
                MouseEvent click = event.getTrigger();
                Rectangle2D dataArea = panel.getScreenDataArea();
                XYPlot plot = (XYPlot) event.getChart().getPlot();
                ValueAxis xAxis = plot.getDomainAxis();
                ValueAxis yAxis = plot.getRangeAxis();
                Vector2D translate = new Vector2D(
                        xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
                                RectangleEdge.BOTTOM),
                        yAxis.java2DToValue(event.getTrigger().getY(), dataArea,
                                RectangleEdge.LEFT));
                dataset.addSeries(getVectorSeries(translate, "Main", 0, 0));
                plot.getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                translate.mapSquareToDiamond().divide(Math.sqrt(2) / 2);

                System.out.println(translate);
                double fr, fl, br, bl;
                fl = translate.dot(frontLeftVec);
                fr = translate.dot(frontRightVec);
                bl = translate.dot(backLeftVec);
                br = translate.dot(backRightVec);


                dataset.addSeries(getVectorSeries(Vector2D.ofDirection(fl, frontLeftVec.getDirection()), "FL", -1, 1));
                plot.getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

                dataset.addSeries(getVectorSeries(Vector2D.ofDirection(fr, frontRightVec.getDirection()), "FR", 1, 1));
                plot.getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

                dataset.addSeries(getVectorSeries(Vector2D.ofDirection(bl, backLeftVec.getDirection()), "BL", -1, -1));
                plot.getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

                dataset.addSeries(getVectorSeries(Vector2D.ofDirection(br, backRightVec.getDirection()), "BR", 1, -1));
                plot.getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

                plot.clearAnnotations();
                List<XYAnnotation> a = new ArrayList<>();
                a.add(new XYShapeAnnotation(new Rectangle(-1, -1, 2, 2)));
                a.add(new XYTextAnnotation(String.valueOf(fl), -1, 1));
                a.add(new XYTextAnnotation(String.valueOf(fr), 1, 1));
                a.add(new XYTextAnnotation(String.valueOf(bl), -1, -1));
                a.add(new XYTextAnnotation(String.valueOf(br), 1, -1));
                a.add(new XYTextAnnotation(String.valueOf(translate.magnitude()), 0, 0));
                a.forEach(plot::addAnnotation);
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {

            }
        });
        add(panel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        OmniDriveVisualize chart = new OmniDriveVisualize("Title");
        chart.pack();
        chart.setVisible(true);
    }

    public XYSeries getVectorSeries(Vector2D vec, String name, double baseX, double baseY) {
        XYSeries series = new XYSeries(name);
        series.add(baseX, baseY);
        series.add(vec.x + baseX, vec.y + baseY);
        return series;
    }


}
