// LinkedList class representing a single Linked List
public class LinkedList {
	Node front, back;
	int size;

	// Constructor to create an empty linked list
	public LinkedList() {
		front = back = null;
		size = 0;
	}

	// Method to return the size of list
	public int size() {
		return size;
	}

	// Method to add an elment to the end of the list
	public void addLast(Object element) {
		Node newNode = new Node(element);
		// If list is empty, make new node front and back of list
		if (size == 0) {
			front = back = newNode;
		} else {
			// Add the new node at the end and update back
			back.next = newNode;
			back = newNode;
		}
		size++;
	}

	// Method to add a new user with ID, name, and age
	public void addUser(int ID, String name, int age) {
		User newUser = new User(ID, name, age);
		addLast(newUser);
	}

	// Method to update an existing user's name and age by ID
	public boolean updateUser(int ID, String newName, int newAge) {
		Node current = front;
		while (current != null) {
			if (current.element instanceof User) {
				User user = (User) current.element;
				// User found and updated
				if (user.getID() == ID) {
					user.setName(newName);
					user.setAge(newAge);
					return true;
				}
			}
			current = current.next;
		}
		// User is not found
		return false;
	}

	// Method to delete a user by his/her ID
	public boolean deleteUserByID(int ID) {
		Node current = front;
		Node prev = null;

		while (current != null) {
			// If the userID is a User node
			if (current.element instanceof User) {
				User user = (User) current.element;
				if (user.getID() == ID) {
					// User found, remove it
					if (prev == null) {
						front = current.next;
					} else {
						prev.next = current.next;
					}
					if (current == back) {
						back = prev;
					}
					size--;
					return true;
				}
				// If the userID is an Integer node (used in sharedWith lists)
			} else if (current.element instanceof Integer) {
				Integer storedID = (Integer) current.element;
				if (storedID == ID) {
					// Remove the ID if found
					if (prev == null) {
						front = current.next;
					} else {
						prev.next = current.next;
					}
					if (current == back) {
						back = prev;
					}
					size--;
					return true;
				}
			}
			prev = current;
			current = current.next;
		}
		// ID is not found
		return false;
	}

	// Method to remove an Integer node by ID
	public void remove(int ID) {
		Node current = front;
		Node prev = null;
		while (current != null) {
			if (current.element instanceof Integer && (Integer) current.element == ID) {
				if (prev == null) {
					front = current.next;
				} else {
					prev.next = current.next;
				}
				if (current == back) {
					back = prev;
				}
				size--;
				return;
			}
			prev = current;
			current = current.next;
		}
	}

	// Overloaded remove method to remove any object by value
	public void remove(Object obj) {
		Node current = front;
		Node prev = null;
		while (current != null) {
			if (current.element.equals(obj)) {
				if (prev == null) {
					front = current.next;
				} else {
					prev.next = current.next;
				}
				if (current == back) {
					back = prev;
				}
				size--;
				return;
			}
			prev = current;
			current = current.next;
		}
	}

	// Method to find a user by his/her ID
	public User findUserByID(int ID) {
		Node current = front;
		while (current != null) {
			if (current.element instanceof User) {
				User user = (User) current.element;
				// Return the user with this ID
				if (user.getID() == ID) {
					return user;
				}
			}
			current = current.next;
		}
		// User with this ID is not found
		return null;
	}

	// Method to find a user by his/her username
	public User findUserByName(String name) {
		Node current = front;
		while (current != null) {
			if (current.element instanceof User) {
				User user = (User) current.element;
				// Name found, return the user
				if (user.getName().equalsIgnoreCase(name)) {
					return user;
				}
			}
			current = current.next;
		}
		// User with this name is not found
		return null;
	}

	// Method to return an element at a given index
	public Object get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}
		Node current = front;
		for (int i = 0; i < index; i++) {
			current = current.next;
		}
		return current.element;
	}

	// Method to check if a certain ID (Integer) exists in the list
	public boolean contains(int ID) {
		Node current = front;
		while (current != null) {
			if (current.element instanceof Integer && (Integer) current.element == ID) {
				return true;
			}
			current = current.getNext();
		}
		return false;
	}

	// Method to print and represent the linked list
	public String printList() {
		if (size == 0) {
			return "Empty";
		}
		StringBuilder s = new StringBuilder();
		Node current = front;
		while (current != null) {
			s.append(current.element.toString());
			if (current.next != null) {
				s.append(" -> ");
			}
			current = current.next;
		}
		return s.toString();
	}
}