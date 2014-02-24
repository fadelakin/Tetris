package tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.Shape.Tetrominoes;

public class Board extends JPanel implements ActionListener {
	final int BoardWidth = 10;
	final int BoardHeight = 22;
	
	Timer timer;
	boolean isFallingFinished = false;
	boolean isStarted = false;
	boolean isPaused = false;
	int numLinesRemoved = 0;
	int curX = 0;
	int curY = 0;
	JLabel statusbar;
	Shape curPiece;
	Tetrominoes[] board;
	
	public Board(Tetris parent) {
		setFocusable(true);
		curPiece = new Shape();
		timer = new Timer(400, this);
		timer.start();
		
		statusbar = parent.getStatusBar();
		board = new Tetrominoes[BoardWidth * BoardHeight];
		addKeyListener(new TAdapter());
		clearBoard();
	}
	
	public void actionPerformed(ActionEvent e) {
		if(isFallingFinished) {
			isFallingFinished = false;
			newPiece();
		} else {
			oneLineDown();
		}
	}
	
	int squareWidth() { return (int) getSize().getWidth() / BoardWidth; }
	int squareHeight() { return (int) getSize().getHeight() / BoardHeight; }
	Tetrominoes shapeAt(int x, int y) { return board[(y * BoardWidth) + x]; }
	
	public void start() {
		if(isPaused)
			return;
		
		isStarted = true;
		isFallingFinished = false;
		numLinesRemoved = 0;
		clearBoard();
		
		newPiece();
		timer.start();
	}
	
	private void pause() {
		if(!isStarted)
			return;
		
		isPaused = !isPaused;
		if(isPaused) {
			timer.stop();
			statusbar.setText("paused");
		} else {
			timer.start();
			statusbar.setText(String.valueOf(numLinesRemoved));
		}
		repaint();
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		Dimension size = getSize();
		int boardTop = (int) size.getHeight() - BoardHeight * squareHeight();
		
		for(int i = 0; i < BoardHeight; i++) {
			for (int j = 0; j < BoardWidth; ++j) {
				Tetrominoes shape = shapeAt(j, BoardHeight - i - 1);
				if (shape != Tetrominoes.NoShape)
					drawSquare(g, 0 + j * squareWidth(),
							boardTop + i * squareHeight(), shape);
			}
		}
		
		if (curPiece.getShape() != Tetrominoes.NoShape){
			for(int i = 0; i < 4; ++i) {
				int x = curX + curPiece.x(i);
				int y = curY - curPiece.y(i);
				drawSquare(g, 0 + x * squareWidth(),
						boardTop + (BoardHeight - y - 1) * squareHeight(),
						curPiece.getShape());
			}
		}
	}
	
	private void dropDown() {
		int newY = curY;
		while (newY > 0) {
			if(!tryMove(curPiece, curX, newY - 1))
				break;
			--newY;
		}
		pieceDropped();
	}
	
	private void oneLineDown() {
		if(!tryMove(curPiece, curX, curY - 1))
			pieceDropped();
	}
	
	private void clearBoard() {
		for(int i = 0; i < BoardHeight * BoardWidth; i++)
			board[i] = Tetrominoes.NoShape;
	}
	
	private void pieceDropped() {
		for(int i = 0; i < 4; i++) {
			int x = curX + curPiece.x(i);
			int y = curY - curPiece.y(i);
			board[(y * BoardWidth) + x] = curPiece.getShape();
		}
		
		removeFullLines();
		
		if (!isFallingFinished)
			newPiece();
	}
	
	private void newPiece() {
		curPiece.setRandomShape();
		curX = BoardWidth / 2 + 1;
		curY = BoardHeight - 1 + curPiece.minY();
		
		if (!tryMove(curPiece, curX, curY)) {
			curPiece.setShape(Tetrominoes.NoShape);
			timer.stop();
			isStarted = false;
			statusbar.setText("Game Over");
		}
	}
	
	private boolean tryMove(Shape newPiece, int newX, int newY) {
		for(int i = 0; i < 4; ++i) {
			int x = newX + newPiece.x(i);
			int y = newY - newPiece.y(i);
			if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight)
				return false;
			if (shapeAt(x, y) != Tetrominoes.NoShape)
				return false;
		}
		
		curPiece = newPiece;
		curX = newX;
		curY = newY;
		repaint();
		return true;
	}
	
	private void removeFullLines() {
		int numFullLines = 0;
		
		for (int i = BoardHeight - 1; i >= 0; --i) {
			boolean lineIsFull = true;
			
			for (int j = 0; j < BoardWidth; ++j) {
				if(shapeAt(j, i) == Tetrominoes.NoShape){
					lineIsFull = false;
					break;
				}
			}
			
			if (lineIsFull) {
				++numFullLines;
				for(int k = i; k < BoardHeight - 1; ++k) {
					for (int j = 0; j < BoardWidth; ++j) {
						board[(k * BoardWidth) + j] = shapeAt(j, k + 1);
					}
				}
			}
		}
	}
}
