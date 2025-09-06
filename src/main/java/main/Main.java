package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Frame extends JFrame {

    private final int frameWidth = 1000;
    private final int frameHeight = 1000;

    private final JToolBar toolBar;
    private final JMenuBar menuBar;
    private final JMenu fileMenu, saveFileAsMenu, colorsMenu, propsMenu;
    private final JMenuItem pngItem, jpgItem, clearItem, changeBackColorItem, changeDrawColorItem, changeDrawLengthItem;

    private final DrawingPanel drawingPanel;
    private final Listener listener;

    public Frame() {
        listener = new Listener();
        drawingPanel = new DrawingPanel();
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        saveFileAsMenu = new JMenu("Save File As: ");
        colorsMenu = new JMenu("Colors");
        changeBackColorItem = new JMenuItem("Change Back Color");
        changeBackColorItem.addActionListener(listener);
        changeDrawColorItem = new JMenuItem("Change Draw Color");
        changeDrawColorItem.addActionListener(listener);
        propsMenu = new JMenu("Draw_Props");
        changeDrawLengthItem = new JMenuItem("Change Draw Length");
        changeDrawLengthItem.addActionListener(listener);
        propsMenu.add(changeDrawLengthItem);
        colorsMenu.add(changeBackColorItem);
        colorsMenu.add(changeDrawColorItem);
        pngItem = new JMenuItem("PNG");
        pngItem.addActionListener(listener);
        jpgItem = new JMenuItem("JPEG");
        jpgItem.addActionListener(listener);
        clearItem = new JMenuItem("Clear");
        clearItem.addActionListener(listener);
        saveFileAsMenu.add(pngItem);
        saveFileAsMenu.add(jpgItem);
        fileMenu.add(saveFileAsMenu);
        fileMenu.add(clearItem);
        menuBar.add(fileMenu);
        menuBar.add(colorsMenu);
        menuBar.add(propsMenu);
        toolBar.add(menuBar);
        add(toolBar, BorderLayout.NORTH);
        add(drawingPanel);
        initFrame();
    }

    private void initFrame() {
        setTitle("Drawing App");
        addKeyListener(listener);
        setSize(frameWidth, frameHeight);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private void saveDrawingAsImage(String imageType, String path) {
        // Create a BufferedImage with the same size as the drawing panel
        int colorsType = imageType.equalsIgnoreCase("JPEG") ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage image = new BufferedImage(drawingPanel.getWidth(), drawingPanel.getHeight(), colorsType);
        Graphics2D g2d = image.createGraphics();
        // Paint the drawing panel onto the BufferedImage
        drawingPanel.paint(g2d);
        g2d.dispose();

        String typeTemp = imageType;
        if(imageType.equals("JPEG")) typeTemp = "JPG";
        // Save the BufferedImage as a PNG file
        try {
            ImageIO.write(image, imageType, new File(path + "." + typeTemp.toLowerCase()));
            JOptionPane.showMessageDialog(this, "Saved Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class Listener implements ActionListener, KeyListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == clearItem) {
                drawingPanel.dots.clear();
                drawingPanel.isLine.clear();
                drawingPanel.repaint();
            }
            else if(e.getSource() == pngItem || e.getSource() == jpgItem) {
                JFileChooser fileChooser = new JFileChooser();
                int userSelection = fileChooser.showSaveDialog(Frame.this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    saveDrawingAsImage(e.getActionCommand(), fileToSave.getAbsolutePath());
                }
            }
            else if(e.getSource() == changeBackColorItem || e.getSource() == changeDrawColorItem) {
                JColorChooser colorChooser = new JColorChooser();
                colorChooser.setColor((e.getSource() == changeBackColorItem)? drawingPanel.backgroundColor: drawingPanel.drawColor);
                int option = JOptionPane.showConfirmDialog(
                  Frame.this,
                        colorChooser,
                        "Choose a Color",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );
                if(option == JOptionPane.OK_OPTION) {
                    Color selectedColor = colorChooser.getColor();
                    if(e.getSource() == changeBackColorItem) drawingPanel.backgroundColor = selectedColor;
                    else if(e.getSource() == changeDrawColorItem) drawingPanel.drawColor = selectedColor;
                    drawingPanel.repaint();
                }
            }
            else if(e.getSource() == changeDrawLengthItem) {
                SpinnerNumberModel model = new SpinnerNumberModel(drawingPanel.drawLength, 1, 50, 1);
                JSpinner drawLengthSpinner = new JSpinner(model);
                int option = JOptionPane.showConfirmDialog(
                        Frame.this,
                        drawLengthSpinner,
                        "Change Draw Length",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );
                if(option == JOptionPane.OK_OPTION) {
                    drawingPanel.drawLength = (int) drawLengthSpinner.getValue();
                    drawingPanel.repaint();
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_Z && e.isControlDown()) {
                if(!(drawingPanel.dots.isEmpty() && drawingPanel.isLine.isEmpty())) {
                    if(drawingPanel.isLine.getLast()) {
                        drawingPanel.dots.removeLast();
                        drawingPanel.dots.removeLast();
                        drawingPanel.isLine.removeLast();
                        drawingPanel.isLine.removeLast();
                    }
                    else {
                        drawingPanel.dots.removeLast();
                        drawingPanel.isLine.removeLast();
                    }
                    drawingPanel.repaint();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
    }

    private class DrawingPanel extends JPanel {

        private Color drawColor = Color.RED;
        private Color backgroundColor = Color.BLACK;
        private boolean isPressed;
        private final DrawingPanelListener drawingPanelListener = new DrawingPanelListener();
        private Graphics2D g2d;
        private final List<Dot> dots = new ArrayList<>();
        private final List<Boolean> isLine = new ArrayList<>();
        private int drawLength;

        private static class Dot {
            private int drawLength;
            private Color drawColor;
            private Point point;

            public Dot(Point point, int drawLength, Color drawColor) {
                this.drawLength = drawLength;
                this.drawColor = drawColor;
                this.point = point;
            }

            public int getDrawLength() {
                return drawLength;
            }

            public Color getDrawColor() {
                return drawColor;
            }

            public Point getPoint() {
                return point;
            }
        }

        public DrawingPanel() {
            setCursorForPanel();
            addMouseListener(drawingPanelListener);
            addMouseMotionListener(drawingPanelListener);
            drawLength = 10;
        }

        private void setCursorForPanel() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image image = toolkit.getImage("images/pencil_icon.png");
            Point hotspot = new Point(0, 25);
            Cursor customCursor = toolkit.createCustomCursor(image, hotspot, "Custom Cursor");
            setCursor(customCursor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(backgroundColor);
            g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            reDrawPoints();
        }

        private void reDrawPoints() {
            for (int i = 0; i < dots.size(); i++) {
                Dot dot = dots.get(i);
                Point point1 = dot.getPoint();
                if (i + 1 < isLine.size() && isLine.get(i + 1)) {
                    Point point2 = dots.get(i + 1).getPoint();
                    drawLine(point1, point2, dot.getDrawLength(), dot.getDrawColor());
                } else {
                    drawDot(point1, dot.getDrawLength(), dot.getDrawColor());
                }
            }
        }

        private void drawLine(Point point1, Point point2, int drawLength, Color drawColor) {
            g2d.setPaint(drawColor);
            g2d.setStroke(new BasicStroke(drawLength));
            g2d.drawLine(point1.x, point1.y, point2.x, point2.y);
        }

        private void drawDot(Point point, int drawLength, Color drawColor) {
            g2d.setPaint(drawColor);
            g2d.fillOval(point.x, point.y, drawLength, drawLength);
        }

        private class DrawingPanelListener extends MouseMotionAdapter implements MouseListener {

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                dots.add(new Dot(e.getPoint(), drawLength, drawColor));
                isLine.add(false);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isPressed = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point point = e.getPoint();
                if (isPressed && contains(point)) {
                    dots.add(new Dot(point, drawLength, drawColor));
                    isLine.add(true);
                    repaint();
                }
            }


            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
        }
    }
}

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Frame::new);
    }
}
