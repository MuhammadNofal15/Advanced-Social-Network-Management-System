// Post classes containing post's information
public class Post {
	private int postID;
	private int creatorID;
	private String content;
	private String date;
	private LinkedList sharedWith;

	// Constructor to create a new post
	public Post(int postID, int creatorID, String content, String date) {
		this.postID = postID;
		this.creatorID = creatorID;
		this.content = content;
		this.date = date;
		this.sharedWith = new LinkedList();
	}

	// Getters and Setters
	public int getPostID() {
		return postID;
	}

	public int getCreatorID() {
		return creatorID;
	}

	public String getContent() {
		return content;
	}

	public String getDate() {
		return date;
	}

	public LinkedList getSharedWith() {
		return sharedWith;
	}

	// Method to add a user's ID and share the post with him
	public void addSharedUser(int userID) {
		sharedWith.addLast(userID);
	}

	// Method to remove a user's ID from a
	public boolean removeSharedUser(int userID) {
		return this.sharedWith.deleteUserByID(userID);
	}

	// Method to get the users who has the post shared with them
	public String getSharedWithString() {
		if (sharedWith != null) {
			return sharedWith.printList();
		} else {
			return "None";
		}
	}

	// Method to print post's info in a formatted way
	@Override
	public String toString() {
		return String.format("Post ID = %d, Creator = User %d, Content = \"%s\", Date = %s, Shared With=[%s]", postID,
				creatorID, content, date, sharedWith.printList());
	}
}