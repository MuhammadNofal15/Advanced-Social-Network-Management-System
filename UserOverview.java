// UserOverview class used to display user's data in FX class
public class UserOverview {
	private int ID;
	private String name;
	private int age;
	private String friends;
	private String posts;

	// Constructor to create a new user overview
	public UserOverview(int ID, String name, int age, String friends, String posts) {
		this.ID = ID;
		this.name = name;
		this.age = age;
		this.friends = friends;
		this.posts = posts;
	}

	// Getters and Setters
	public int getId() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public String getFriends() {
		return friends;
	}

	public String getPosts() {
		return posts;
	}
}