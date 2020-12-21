package de.maumau.engine;

public interface Stack {
    public boolean isEmpty();
    public boolean isInside(int karte);
    public void push(int karte);
    public void pop();
    public int peek();
    public int size();
}
