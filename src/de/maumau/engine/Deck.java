package de.maumau.engine;

/**
 *
 * @author Barny
 */
public class Deck implements Stack {

    private int[] stack;
    
    public Deck() {
        stack = new int[0];
    }
    
    @Override
    public boolean isEmpty() {
        return stack.length == 0;
    }
    
    @Override
    public boolean isInside(int karte) {
        if(!isEmpty()) {
            for(int i=0;i<stack.length;i++) {
                if(karte == stack[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void push(int karte) {
        stack = incStack(stack, karte);
    }

    @Override
    public void pop() {
        if(!isEmpty() && stack.length > 0) {
            stack = decStack(stack);
        }
    }

    @Override
    public int peek() {
        if(!isEmpty()) {
            return stack[stack.length-1];
        }else {
            return -1;
        }
    }
    
    @Override
    public int size() {
    	return stack.length;
    }
    
    private int[] incStack(int[] arr, int karte) {
        int[] temp = new int[arr.length+1];
        
        for(int i=0;i<arr.length;i++) {
            temp[i] = arr[i];
        }
        
        temp[temp.length-1] = karte;
        
        return temp;
    }
    
    private int[] decStack(int[] arr) {
        int[] temp = new int[arr.length-1];
        
        for(int i=0;i<temp.length;i++) {
            temp[i] = arr[i];
        }
        
        return temp;
    }
    
}
