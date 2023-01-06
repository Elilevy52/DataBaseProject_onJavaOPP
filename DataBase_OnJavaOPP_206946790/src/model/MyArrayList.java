package model;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class MyArrayList {
	public static final int MAX = 200;
	private Question[] array = new Question[MAX];
	private int size = 0;
	
	public void add(Question quest) {
		if (size < MAX) {
			array[size++] = quest;
		}
		else {
			throw new IllegalArgumentException("No more place");
		}
	}
	
	public Iterator<Question> iterator(){
		return new ConcreteIterrator();
	}
	private class ConcreteIterrator implements Iterator<Question> {
		private int cur = 0;
		@Override
		public boolean hasNext() {
			return cur < size;
		}

		@Override
		public Question next() {
			if(!hasNext()) {
				throw new NoSuchElementException();
			}
			Question tmp = array[cur++];
			return tmp;
		}
		@Override
		public void remove() {
			for(int i = cur - 1; i < size; i++) {
				array[i] = array[i+1];
			}
			array[size--] = null; 
		}
	}
}
