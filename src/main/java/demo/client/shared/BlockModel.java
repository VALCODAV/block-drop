package demo.client.shared;

import java.util.Iterator;

/*
 * The base class for a falling block in a Block Drop BoardModel.
 */
public class BlockModel implements Iterable<Integer[]> {
	
	/*
	 * For iterating through the positions of squares in a single block.
	 */
	public class SquareIterator implements Iterator<Integer[]> {

		/* Index of next position to return. */
		private int next;
		
		/*
		 * Create a SquareIterator.
		 * 
		 * @param offsets The offsets of each square to iterate through.
		 */
		public SquareIterator() {
			super();
			next = 0;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return next < offsets.length;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer[] next() {
			Integer[] res = new Integer[] {
					new Integer(offsets[next][0] + mainPosition[0]),
					new Integer(offsets[next][1] + mainPosition[1])};
			next++;
			return res;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public static final int BASIC_CODE = 1;
	
	private static int idGen = 1;

	/* 
	 * An array of pairs. Represents the offset positions of each tile in this block
	 * from the central position.
	 */
	private int[][] offsets;
	/* The main index of this piece on the Block Drop board. */
	private int[] mainPosition;
	/* A unique id for identifying this block. */
	private int id;
	
	/*
	 * Create a basic BlockModel consisting of one square.
	 * 
	 * @param rowNum The number of rows in the board (used to determine starting position).
	 * @param colNum The number of columns in the board (used to determine starting position).
	 */
	public BlockModel() {
		// Creates array {{0,0}}, so single square with no offset.
		offsets = new int[1][2];
		
		// Start piece above board
		mainPosition = new int[] {-1, BoardModel.COL_NUM/2};
		
		id = generateId();
	}
	
	public boolean equals(Object other) {
		return other.getClass() == BlockModel.class && this.getId() == ((BlockModel) other).getId();
	}
	
	public int getId() {
		return id;
	}

	private static int generateId() {
		return idGen++;
	}

	/*
	 * Get the integer representing this type of block on the board.
	 * 
	 * @return An integer representing this type of block on the board.
	 */
	public int getCode() {
		return BASIC_CODE;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer[]> iterator() {
		return new SquareIterator();
	}
	
	public void rotateClockwise() {
		//TODO: Implement clockwise rotation.
	}
	
	public void rotateCounterclockwise() {
		//TODO: Implement counter-clockwise rotation.
	}
	
	/*
	 * Lower position of block by 1 square.
	 */
	public void lowerPosition() {
		setMainPosition(mainPosition[0]+1, mainPosition[1]);
	}
	
	/*
	 * Set the position of this block.
	 */
	private void setMainPosition(int row, int col) {
		mainPosition[0] = row;
		mainPosition[1] = col;
	}
}
