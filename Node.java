// Single Node class
public class Node {
	Object element;
	Node next;

	// Constructor to create a new node
	public Node(Object element) {
		this.element = element;
		this.next = null;
	}

	// Getters and Setters for the Node class
	public Object getElement() {
		return element;
	}

	public void setElement(Object element) {
		this.element = element;
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}
}