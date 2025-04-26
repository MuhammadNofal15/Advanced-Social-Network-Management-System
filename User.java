// User class containing user's personal data
public class User {
	private int ID;
	private String name;
	private int age;
	private LinkedList friends;

	// Constructor to create a new user
	public User(int ID, String name, int age) {
		this.ID = ID;
		this.name = name;
		this.age = age;
		this.friends = new LinkedList();
	}

	// Getters and Setters
	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public LinkedList getFriends() {
		return friends;
	}

	public void setFriends(LinkedList friends) {
		this.friends = friends;
	}

	// Method to add a friend's user ID to the friends list
	public void addFriend(int friendID) {
		friends.addLast(friendID);
	}

	// Method to add another user who shares something in common to the friends list
	public void addSharedUser(int userID) {
		friends.addLast(userID);
	}

	// Method to print user's info in a formatted way
	@Override
	public String toString() {
		return String.format("User ID = %d, Name = '%s', Age = %d, Friends = [%s]", ID, name, age, friends.printList());
	}
}