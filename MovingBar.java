package com.ggl.testing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class MovingBar implements Runnable {

	public static void main(String[] args) {
		System.setProperty("sun.java2d.uiScale", "1");
		SwingUtilities.invokeLater(new MovingBar());
	}

	private DrawingPanel drawingPanel;

	private JTextField averageField, pointsField, turnsField;

	private final MovingBarModel model;

	public MovingBar() {
		this.model = new MovingBarModel();
		this.drawingPanel = new DrawingPanel(this, model);
	}

	@Override
	public void run() {
		JFrame frame = new JFrame("Moving Bar");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(createControlPanel(), BorderLayout.NORTH);
		frame.add(drawingPanel, BorderLayout.CENTER);
		frame.add(createPointsPanel(), BorderLayout.SOUTH);

		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	private JPanel createControlPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font labelFont = panel.getFont().deriveFont(Font.BOLD, 24f);
		Font textFont = panel.getFont().deriveFont(Font.PLAIN, 24f);

		JLabel label = new JLabel("Points:");
		label.setFont(labelFont);
		panel.add(label);

		pointsField = new JTextField(3);
		pointsField.setEditable(false);
		pointsField.setFont(textFont);
		pointsField.setHorizontalAlignment(JTextField.TRAILING);
		panel.add(pointsField);

		panel.add(Box.createHorizontalStrut(50));

		label = new JLabel("Turns:");
		label.setFont(labelFont);
		panel.add(label);

		turnsField = new JTextField(3);
		turnsField.setEditable(false);
		turnsField.setFont(textFont);
		turnsField.setHorizontalAlignment(JTextField.TRAILING);
		panel.add(turnsField);

		panel.add(Box.createHorizontalStrut(50));

		label = new JLabel("Average:");
		label.setFont(labelFont);
		panel.add(label);

		averageField = new JTextField(5);
		averageField.setEditable(false);
		averageField.setFont(textFont);
		averageField.setHorizontalAlignment(JTextField.TRAILING);
		panel.add(averageField);

		updateControlPanel();

		return panel;
	}

	public void updateControlPanel() {
		String commaFormat = "%,d";
		pointsField.setText(String.format(commaFormat, model.getPoints()));
		int turns = model.getTurns();
		turnsField.setText(String.format(commaFormat, turns));
		averageField.setText(String.format("%.3f", model.calculateAverage()));
	}

	private JPanel createPointsPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font labelFont = panel.getFont().deriveFont(Font.BOLD, 24f);

		String text = "Success - 1 point; bonus - 2 points; big bonus - 4 points";
		JLabel label = new JLabel(text);
		label.setFont(labelFont);
		panel.add(label);

		return panel;
	}

	public void repaintDrawingPanel() {
		drawingPanel.repaint();
	}

	public class DrawingPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private final BufferedImage bigBonus, bonus, pointer, success;

		private final MovingBar view;

		private final MovingBarModel model;

		public DrawingPanel(MovingBar view, MovingBarModel model) {
			this.view = view;
			this.model = model;
			this.setBackground(Color.BLACK);

			int widthMargin = model.getWidthMargin();
//			int heightMargin = model.getHeightMargin();
			Bar[] bars = model.getBars();
			Bar bar = bars[bars.length - 1];
			Rectangle r = bar.getRectangle();
			int width = r.x + r.width + r.width;
			int height = r.y + r.height + widthMargin;
			this.setPreferredSize(new Dimension(width, height));

			setKeyBindings(this);
			this.pointer = createPointer();
			this.success = createDisplayImage(Color.YELLOW, "SUCCESS", width, height);
			this.bonus = createDisplayImage(Color.GREEN, "BONUS", width, height);
			this.bigBonus = createDisplayImage(Color.BLUE, "BIG BONUS", width, height);
		}

		private void setKeyBindings(DrawingPanel drawingPanel) {
			InputMap inputMap = drawingPanel
					.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
			ActionMap actionMap = drawingPanel.getActionMap();

			String key1 = "space";
			String key2 = "release";
			inputMap.put(KeyStroke.getKeyStroke("SPACE"), key1);
			inputMap.put(KeyStroke.getKeyStroke("released SPACE"), key2);
			actionMap.put(key1, new PointerAction(view, model));
			actionMap.put(key2, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					model.setRunning(false);
				}
			});
		}

		private BufferedImage createPointer() {
			int barWidth = model.getBarWidth();
			int barHeight = model.getBarHeight();
			int heightMargin = model.getHeightMargin();
			int imageHeight = barHeight + heightMargin;
			BufferedImage image = new BufferedImage(barWidth, imageHeight,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			Color color = new Color(0, 0, 0, 0);
			g.setColor(color);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());

			g.setColor(Color.RED);
			g.fillRect(image.getWidth() / 2 - 1, 0, 3, imageHeight);

			Polygon p = new Polygon();
			p.addPoint(0, 0);
			p.addPoint(image.getWidth(), 0);
			p.addPoint(image.getWidth() / 2, heightMargin);
			g.fillPolygon(p);

			g.dispose();

			return image;
		}

		private BufferedImage createDisplayImage(Color color, String text,
				int width, int height) {
			int heightMargin = model.getHeightMargin();
			int rHeight = height - heightMargin;
			BufferedImage image = new BufferedImage(width, rHeight,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			Color bColor = new Color(0, 0, 0, 64);
			g.setColor(bColor);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			
			Font font = getFont().deriveFont(Font.BOLD, 48f);
			g.setFont(font);
			
			FontMetrics fontMetrics = g.getFontMetrics();
			int fWidth = fontMetrics.stringWidth(text);
	        int fHeight = fontMetrics.getHeight();
	        g.setColor(color);
	        g.fillRect(0, heightMargin, width, fHeight);
	        int x = (width - fWidth) / 2;
	        int y = heightMargin + fHeight - fontMetrics.getDescent();
	        g.setColor(Color.BLACK);
	        if (color.equals(Color.BLUE)) {
	        	g.setColor(Color.WHITE);
	        }
	        g.drawString(text, x, y);
	        
			g.dispose();
			
			return image;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Bar[] bars = model.getBars();
			for (Bar bar : bars) {
				g.setColor(bar.getColor());
				Rectangle r = bar.getRectangle();
				g.fillRect(r.x, r.y, r.width, r.height);
			}

			if (model.getState() != State.INITIAL) {
				int widthMargin = model.getWidthMargin();
				int xPosition = model.getxPosition() - pointer.getWidth() / 2;
				g.drawImage(pointer, xPosition, widthMargin, this);
			}
			
			if (model.getState() == State.MOVING) {
				int currentPoints = model.getCurrentPoints();
				int heightMargin = model.getHeightMargin();
				if (currentPoints == 4) {
					g.drawImage(bigBonus, 0, heightMargin, this);
				} else if (currentPoints == 2) {
					g.drawImage(bonus, 0, heightMargin, this);
				} else {
					g.drawImage(success, 0, heightMargin, this);
				}
			}
		}
		
	}

	public class PointerAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private final MovingBar view;

		private final MovingBarModel model;

		private final Timer timer;

		public PointerAction(MovingBar view, MovingBarModel model) {
			this.view = view;
			this.model = model;
			int time = model.getTimerTime();
			this.timer = new Timer(time, new PointerListener(view, model));
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if (model.isRunning) {
				return;
			}

			model.setRunning(true);
			if (model.getState() == State.MOVING) {
				timer.start();
				model.reset();
				model.randomBonus();
			} else if (model.getState() == State.DISPLAY) {
				timer.stop();
				determinePoints();
				model.incrementTurn();
				view.updateControlPanel();
				view.repaintDrawingPanel();
			} else {
				model.incrementState();
				timer.start();
				model.randomBonus();
			}
//			System.out.println(model.getState());
			model.incrementState();
		}

		private void determinePoints() {
			Bar bar = model.getSelectedBar();
			Color color = bar.getColor();
			if (color.equals(Color.BLUE)) {
				model.incrementPoints(4);
			} else if (color.equals(Color.GREEN)) {
				model.incrementPoints(2);
			} else {
				model.incrementPoints(1);
			}
		}

	}

	public class PointerListener implements ActionListener {

		private final MovingBar view;

		private final MovingBarModel model;

		public PointerListener(MovingBar view, MovingBarModel model) {
			this.view = view;
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			model.incrementxPosition();
			view.repaintDrawingPanel();
		}

	}

	public class MovingBarModel {

		private boolean isRunning;

		private int currentPoints, direction, points, turns;
		private int xPosition, xStartPosition, xStart, xEnd;
		private final int widthMargin, heightMargin, barWidth, barHeight;
		private final int timerTime;

		private final Bar[] bars;

		private final Random random;

		private State state;

		public MovingBarModel() {
			this.isRunning = false;
			this.points = 0;
			this.turns = 0;
			this.direction = 25;
			this.widthMargin = 20;
			this.heightMargin = 100;
			this.barWidth = 50;
			this.barHeight = 200;
			this.timerTime = 15;
			this.state = State.INITIAL;
			this.random = new Random();
			this.bars = createBars();
		}

		private Bar[] createBars() {
			Bar[] bars = new Bar[20];
			int x = barWidth;
			int y = heightMargin + widthMargin;
			this.xStart = x;
			for (int index = 0; index < bars.length; index++) {
				bars[index] = new Bar(Color.YELLOW,
						new Rectangle(x, y, barWidth, barHeight));
				x += barWidth + 8;
			}
			this.xEnd = x - 8;
			this.xStartPosition = (xEnd - xStart) / 2 + xStart;
			this.xPosition = xStartPosition;

			return bars;
		}

		public void reset() {
			this.xPosition = xStartPosition;
			this.direction = 25;
		}

		public void randomBonus() {
			for (int index = 0; index < bars.length; index++) {
				bars[index].setColor(Color.YELLOW);
			}
			int greenRandom = random.nextInt(bars.length - 5) + 2;
			bars[greenRandom].setColor(Color.GREEN);
			bars[greenRandom + 1].setColor(Color.GREEN);

			int blueRandom = 0;
			do {
				blueRandom = random.nextInt(bars.length - 3) + 1;
			} while (blueRandom == greenRandom
					|| blueRandom == (greenRandom + 1));
			bars[blueRandom].setColor(Color.BLUE);
		}

		public Bar getSelectedBar() {
			int index = getSelectedBar(xPosition);
			if (index < 0) {
				index = getSelectedBar(xPosition + direction);
			}

			Bar bar = bars[index];
			Rectangle r = bar.getRectangle();
			xPosition = r.x + r.width / 2;

			return bars[index];
		}

		private int getSelectedBar(int xPosition) {
			for (int index = 0; index < bars.length; index++) {
				Rectangle r = bars[index].getRectangle();
				if (r.x <= xPosition && xPosition <= (r.x + r.width)) {
					return index;
				}
			}

			return -1;
		}

		public double calculateAverage() {
			if (turns > 0) {
				return (double) points / turns;
			} else {
				return 0.0;
			}
		}

		public void incrementTurn() {
			this.turns++;
		}

		public void incrementxPosition() {
			xPosition += direction;
			if (xPosition >= xEnd) {
				direction = -direction;
			}
			if (xPosition <= xStart) {
				direction = -direction;
			}
//			System.out.println("xPosition = " + xPosition);
		}

		public void incrementPoints(int points) {
			this.currentPoints = points;
			this.points += points;
		}

		public void incrementState() {
			int index = (state.ordinal() + 1) % State.values().length;
			index = (index == 0) ? index + 1 : index;
			this.state = State.values()[index];
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}

		public int getTimerTime() {
			return timerTime;
		}

		public int getWidthMargin() {
			return widthMargin;
		}

		public int getHeightMargin() {
			return heightMargin;
		}

		public int getBarWidth() {
			return barWidth;
		}

		public int getBarHeight() {
			return barHeight;
		}

		public Bar[] getBars() {
			return bars;
		}

		public int getxStart() {
			return xStart;
		}

		public int getxEnd() {
			return xEnd;
		}

		public int getxPosition() {
			return xPosition;
		}

		public int getCurrentPoints() {
			return currentPoints;
		}

		public int getPoints() {
			return points;
		}

		public int getTurns() {
			return turns;
		}

		public State getState() {
			return state;
		}

	}

	public class Bar {

		private Color color;

		private final Rectangle rectangle;

		public Bar(Color color, Rectangle rectangle) {
			this.color = color;
			this.rectangle = rectangle;
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public Rectangle getRectangle() {
			return rectangle;
		}

	}

	public enum State {
		INITIAL, MOVING, DISPLAY;
	}

}
