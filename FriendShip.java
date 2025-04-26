// FriendShip class showing user's ID and his/her friends
public class FriendShip {
	private int userID;
	private String friends;

	// Constructor to add a new friend to a specific user
	public FriendShip(int userID, String friends) {
		this.userID = userID;
		this.friends = friends;
	}

	// Getters and Setters
	public int getUserID() {
		return userID;
	}

	public String getFriends() {
		return friends;
	}
}