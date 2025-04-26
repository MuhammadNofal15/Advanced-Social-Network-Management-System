import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Optional;

public class SocialNetworkManagerFX extends Application {

	private LinkedList users = new LinkedList();
	private LinkedList posts = new LinkedList();
	private boolean usersLoaded = false;
	private boolean friendshipsLoaded = false;
	private boolean postsLoaded = false;
	private int currentUserIndex = 0;
	private int currentPage = 0;
	private final int pageSize = 10;
	private ObservableList<UserOverview> fullList;
	private TableView<UserOverview> overviewTable;

	@Override
	public void start(Stage stage) {
		Text title = new Text("Welcome to Social Network Manager");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		title.setFill(Color.DARKSLATEBLUE);

		Text subtitle = new Text("Start by loading the files from left to right");
		subtitle.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
		subtitle.setFill(Color.GRAY);

		// Load buttons
		Button loadUsers = new Button("Load Users");
		Button loadFriendships = new Button("Load Friendships");
		Button loadPosts = new Button("Load Posts");

		loadUsers.setPrefWidth(120);
		loadFriendships.setPrefWidth(140);
		loadPosts.setPrefWidth(120);

		loadUsers.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
		loadFriendships.setStyle("-fx-font-size: 14px; -fx-background-color: #2196F3; -fx-text-fill: white;");
		loadPosts.setStyle("-fx-font-size: 14px; -fx-background-color: #FF9800; -fx-text-fill: white;");

		loadUsers.setOnAction(e -> loadProfiles(stage));
		loadFriendships.setOnAction(e -> loadFriendships(stage));
		loadPosts.setOnAction(e -> loadPosts(stage));

		// Button layout
		HBox buttonLayout = new HBox(20, loadUsers, loadFriendships, loadPosts);
		buttonLayout.setAlignment(Pos.CENTER);
		buttonLayout.setPadding(new Insets(20));

		// Main vertical layout
		VBox layout = new VBox(15, title, subtitle, buttonLayout);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(30));
		layout.setStyle("-fx-background-color: #f0f8ff;");

		// Set scene and stage
		Scene scene = new Scene(layout, 550, 300);
		stage.setTitle("Social Network Manager");
		stage.setScene(scene);
		stage.show();
	}

	// Method to Loads user profiles from a file
	private void loadProfiles(Stage stage) {
		File file = openFile(stage);

		if (file != null) {
			users = new LinkedList();

			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				// Read the header line and check if it matches the correct header
				String header = br.readLine();
				if (header == null || !header.trim().equalsIgnoreCase("User ID,Name,Age")) {
					showAlert("Invalid File", "This file is not a valid users file.", Alert.AlertType.ERROR);
					return;
				}

				String line;
				// Read each line and parse user data
				while ((line = br.readLine()) != null) {
					try {
						String[] parts = line.split(",");
						if (parts.length == 3) {
							int id = Integer.parseInt(parts[0].trim());
							String name = parts[1].trim();
							int age = Integer.parseInt(parts[2].trim());
							users.addLast(new User(id, name, age));
						}
					} catch (Exception ignored) {
					}
				}

				// Users loaded successfully
				usersLoaded = true;
				checkAllFilesLoaded(stage);

				// For any errors when reading the profile file
			} catch (IOException e) {
				showAlert("Error", "Failed to read profiles file: " + e.getMessage(), Alert.AlertType.ERROR);
			}
		}
	}

	// Method to load friendships between users from a file
	private void loadFriendships(Stage stage) {
		File file = openFile(stage);

		if (file != null) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				// Read the header line and check if it matches the correct header
				String header = br.readLine();
				if (header == null || !header.trim().equalsIgnoreCase("User ID,Friends")) {
					showAlert("Invalid File", "This file is not a valid friendships file.", Alert.AlertType.ERROR);
					return;
				}

				String line;
				// Read each line and link friends to the user
				while ((line = br.readLine()) != null) {
					try {
						String[] parts = line.split(",");
						if (parts.length > 1) {
							User user = findUserByID(Integer.parseInt(parts[0].trim()));
							if (user != null) {
								for (int i = 1; i < parts.length; i++) {
									user.addFriend(Integer.parseInt(parts[i].trim()));
								}
							}
						}
					} catch (Exception ignored) {
					}
				}

				// Friendships loaded successfully
				friendshipsLoaded = true;
				checkAllFilesLoaded(stage);

				// For any errors when reading the friendship file
			} catch (IOException e) {
				showAlert("Error", "Failed to read friendships file: " + e.getMessage(), Alert.AlertType.ERROR);
			}
		}
	}

	// Method to load user posts from a file
	private void loadPosts(Stage stage) {
		File file = openFile(stage);

		if (file != null) {
			posts = new LinkedList();

			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				// Read the header line and check if it matches the correct header
				String header = br.readLine();
				if (header == null
						|| !header.trim().equalsIgnoreCase("Post ID,Creator ID,Content,Creation Date,Shared With")) {
					showAlert("Invalid File", "This file is not a valid posts file.", Alert.AlertType.ERROR);
					return;
				}

				String line;
				// Read and create posts from each line
				while ((line = br.readLine()) != null) {
					try {
						String[] parts = line.split(",", 5);
						if (parts.length >= 4) {
							int postId = Integer.parseInt(parts[0].trim());
							int creatorId = Integer.parseInt(parts[1].trim());
							String content = parts[2].trim();
							String date = parts[3].trim();

							Post post = new Post(postId, creatorId, content, date);

							// Parse shared users if available
							if (parts.length == 5) {
								String[] sharedUsers = parts[4].split(",");
								for (int i = 0; i < sharedUsers.length; i++) {
									post.addSharedUser(Integer.parseInt(sharedUsers[i].trim()));
								}
							}
							posts.addLast(post);
						}
					} catch (Exception ignored) {
					}
				}

				// Posts loaded successfully
				postsLoaded = true;
				checkAllFilesLoaded(stage);

				// For any errors when reading the posts file
			} catch (IOException e) {
				showAlert("Error", "Failed to read posts file: " + e.getMessage(), Alert.AlertType.ERROR);
			}
		}
	}

	// Method to load the files using a filechooser
	private File openFile(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select File");
		return fileChooser.showOpenDialog(stage);
	}

	private User findUserByID(int userID) {
		Node current = users.front;
		while (current != null) {
			User user = (User) current.element;

			if (user.getID() == userID) {
				return user;
			}
			current = current.getNext();
		}
		return null;
	}

	// Method to check if all files are loaded successfully
	private void checkAllFilesLoaded(Stage stage) {
		if (usersLoaded && friendshipsLoaded && postsLoaded) {
			// Switch to the second interface
			homeInterface(stage);
		}
	}

	// Show the main interface with data users and options
	private void homeInterface(Stage stage) {
		TabPane tabPane = new TabPane();

		// Setup the Home tab
		Tab homeTab = new Tab("Home");
		homeTab.setClosable(false);

		// Main title label
		Label titleLabel = new Label("Social Network Manager");
		titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
		titleLabel.setTextAlignment(TextAlignment.CENTER);

		// Section subtitle
		Label subtitleLabel = new Label("All Users Data:");
		subtitleLabel.setStyle("-fx-font-size: 18px; -fx-font-style: italic;");
		subtitleLabel.setTextFill(Color.DARKGRAY);
		subtitleLabel.setTextAlignment(TextAlignment.CENTER);

		// Info labels for user data
		Label nameLabel = new Label();
		Label friendsLabel = new Label();
		Label postsLabel = new Label();
		Label sharedWithLabel = new Label();

		Label[] labels = { nameLabel, friendsLabel, postsLabel, sharedWithLabel };
		for (int i = 0; i < labels.length; i++) {
			labels[i].setWrapText(true);
			labels[i].setMaxWidth(800);
			labels[i].setStyle("-fx-font-size: 14px; -fx-padding: 5;");
		}

		// Put each label in an HBox and center it
		HBox nameBox = new HBox(nameLabel);
		HBox friendsBox = new HBox(friendsLabel);
		HBox postsBox = new HBox(postsLabel);
		HBox sharedBox = new HBox(sharedWithLabel);

		nameBox.setAlignment(Pos.CENTER);
		friendsBox.setAlignment(Pos.CENTER);
		postsBox.setAlignment(Pos.CENTER);
		sharedBox.setAlignment(Pos.CENTER);

		// VBox to hold user info
		VBox infoBox = new VBox(10, nameBox, friendsBox, postsBox, sharedBox);
		infoBox.setAlignment(Pos.CENTER);
		infoBox.setPadding(new Insets(10, 0, 10, 0));

		// Top content
		VBox topContent = new VBox(20, titleLabel, subtitleLabel, infoBox);
		topContent.setAlignment(Pos.TOP_CENTER);
		topContent.setStyle("-fx-padding: 30 30 10 30;");

		ScrollPane scrollPane = new ScrollPane(topContent);
		scrollPane.setFitToWidth(true);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setPrefHeight(500);

		// Navigation buttons
		Button prevHomeButton = new Button("Previous");
		Button nextHomeButton = new Button("Next");

		prevHomeButton.setStyle("-fx-font-size: 14px;");
		nextHomeButton.setStyle("-fx-font-size: 14px;");

		HBox navBox = new HBox(20, prevHomeButton, nextHomeButton);
		navBox.setAlignment(Pos.CENTER);
		navBox.setPadding(new Insets(15));
		navBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: lightgray; -fx-border-width: 1 0 0 0;");

		// Final home layout
		VBox homeLayout = new VBox(scrollPane, navBox);
		homeLayout.setAlignment(Pos.TOP_CENTER);

		homeTab.setContent(homeLayout);

		currentUserIndex = 0;
		final int totalUsers = users.size();
		updateHomeTab(nameLabel, friendsLabel, postsLabel, sharedWithLabel);

		// Navigation buttons in home tab
		prevHomeButton.setOnAction(e -> {
			if (currentUserIndex > 0) {
				currentUserIndex--;
				updateHomeTab(nameLabel, friendsLabel, postsLabel, sharedWithLabel);
			}
		});

		nextHomeButton.setOnAction(e -> {
			if (currentUserIndex < totalUsers - 1) {
				currentUserIndex++;
				updateHomeTab(nameLabel, friendsLabel, postsLabel, sharedWithLabel);
			}
		});

		// TableView tab
		Tab tableTab = new Tab("TableView");
		tableTab.setClosable(false);

		Label tableTitle = new Label("User Overview Table");
		tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		tableTitle.setTextFill(Color.DARKSLATEBLUE);

		// TableView setup
		overviewTable = new TableView<>();
		overviewTable.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc;");
		overviewTable.setPrefHeight(400);

		// Table columns
		TableColumn<UserOverview, Integer> IDCol = new TableColumn<>("User ID");
		IDCol.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn<UserOverview, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setMinWidth(125);

		TableColumn<UserOverview, Integer> ageCol = new TableColumn<>("Age");
		ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));

		TableColumn<UserOverview, String> friendsCol = new TableColumn<>("Friends");
		friendsCol.setCellValueFactory(new PropertyValueFactory<>("friends"));
		friendsCol.setMinWidth(400);

		TableColumn<UserOverview, String> postsCol = new TableColumn<>("Posts");
		postsCol.setCellValueFactory(new PropertyValueFactory<>("posts"));
		postsCol.setMinWidth(920);

		// Add data to the columns
		overviewTable.getColumns().addAll(IDCol, nameCol, ageCol, friendsCol, postsCol);
		fullList = getUserOverviewList();
		updateTable();

		// Navigation buttons for tableview
		Button prevTableButton = new Button("Previous");
		Button nextTableButton = new Button("Next");

		prevTableButton.setStyle("-fx-font-size: 13px; -fx-background-color: #607D8B; -fx-text-fill: white;");
		nextTableButton.setStyle("-fx-font-size: 13px; -fx-background-color: #607D8B; -fx-text-fill: white;");

		prevTableButton.setOnAction(e -> {
			if (currentPage > 0) {
				currentPage--;
				updateTable();
			}
		});
		nextTableButton.setOnAction(e -> {
			if ((currentPage + 1) * pageSize < fullList.size()) {
				currentPage++;
				updateTable();
			}
		});

		HBox tableNav = new HBox(15, prevTableButton, nextTableButton);
		tableNav.setAlignment(Pos.CENTER);

		// Action buttons
		Button addUserBtn = new Button("Add User");
		Button updateUserBtn = new Button("Update User");
		Button deleteUserBtn = new Button("Delete User");
		Button searchUserBtn = new Button("Search User");

		Button[] buttons = { addUserBtn, updateUserBtn, deleteUserBtn, searchUserBtn };
		for (Button btn : buttons) {
			btn.setStyle("-fx-font-size: 13px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
			btn.setPrefWidth(120);
		}

		addUserBtn.setOnAction(e -> addUser());
		updateUserBtn.setOnAction(e -> updateUser());
		deleteUserBtn.setOnAction(e -> deleteUser());
		searchUserBtn.setOnAction(e -> searchUser());

		HBox actionButtons = new HBox(15, addUserBtn, updateUserBtn, deleteUserBtn, searchUserBtn);
		actionButtons.setAlignment(Pos.CENTER);

		VBox tableLayout = new VBox(20, tableTitle, overviewTable, tableNav, actionButtons);
		tableLayout.setAlignment(Pos.TOP_CENTER);
		tableLayout.setPadding(new Insets(20));
		tableLayout.setStyle("-fx-background-color: #f9f9f9;");
		tableLayout.setPrefWidth(900);

		tableTab.setContent(tableLayout);

		// Menus setup
		MenuBar menuBar = new MenuBar();

		Menu manageMenu = new Menu("Manage");
		Menu saveMenu = new Menu("Save");
		Menu statsMenu = new Menu("Statistics");

		MenuItem manageFriendsItem = new MenuItem("Add/Delete a Friend");
		manageFriendsItem.setOnAction(e -> manageFriends());

		MenuItem managePostsItem = new MenuItem("Add a New Post");
		managePostsItem.setOnAction(e -> addPost());

		MenuItem viewSharedPostsItem = new MenuItem("View Shared Posts");
		viewSharedPostsItem.setOnAction(e -> showSharedPosts());

		MenuItem deletePostsItem = new MenuItem("Delete a Post From the System");
		deletePostsItem.setOnAction(e -> deletePosts());

		MenuItem deletePostsFromViewItem = new MenuItem("Delete a Post From a User's View");
		deletePostsFromViewItem.setOnAction(e -> removeSharedPost());

		MenuItem userPostsItem = new MenuItem("View a User's Posts");
		userPostsItem.setOnAction(e -> showUserPosts());

		MenuItem sharedWithUserItem = new MenuItem("View Posts Shared With a User");
		sharedWithUserItem.setOnAction(e -> showPostsSharedWithUser());

		MenuItem viewMostActiveUsersItem = new MenuItem("View Most Active Users");
		viewMostActiveUsersItem.setOnAction(e -> showMostActiveUsers());

		MenuItem viewEngagementMetricsItem = new MenuItem("View Engagement Metrics");
		viewEngagementMetricsItem.setOnAction(e -> showEngagementMetrics());

		MenuItem viewUserFriendsItem = new MenuItem("View All Friends of a Specific User");
		viewUserFriendsItem.setOnAction(e -> showUserFriends());

		MenuItem savePostsItem = new MenuItem("Save Created Posts");
		savePostsItem.setOnAction(e -> savePostsToFile());

		MenuItem saveSharedPostsItem = new MenuItem("Save Shared Posts");
		saveSharedPostsItem.setOnAction(e -> saveSharedPostToFile());

		MenuItem saveAllDataItem = new MenuItem("Save All Data");
		saveAllDataItem.setOnAction(e -> saveAllData());

		manageMenu.getItems().addAll(manageFriendsItem, managePostsItem, viewSharedPostsItem, deletePostsItem,
				deletePostsFromViewItem);

		statsMenu.getItems().addAll(userPostsItem, sharedWithUserItem, viewMostActiveUsersItem,
				viewEngagementMetricsItem, viewUserFriendsItem);

		saveMenu.getItems().addAll(savePostsItem, saveSharedPostsItem, saveAllDataItem);

		menuBar.getMenus().addAll(manageMenu, statsMenu, saveMenu);

		tabPane.getTabs().addAll(homeTab, tableTab);
		tabPane.getSelectionModel().select(homeTab);

		VBox rootLayout = new VBox(menuBar, tabPane);

		// Setup and show the stage
		Scene scene = new Scene(rootLayout, 950, 600);
		stage.setScene(scene);
		stage.setTitle("Social Network Manager");
		stage.show();
	}

	private void updateHomeTab(Label nameLabel, Label friendsLabel, Label postsLabel, Label sharedWithLabel) {
		Node current = users.front;
		int index = 0;

		while (current != null && index < currentUserIndex) {
			current = current.getNext();
			index++;
		}

		if (current != null) {
			User user = (User) current.element;
			nameLabel.setText(user.getName() + ":");

			StringBuilder friendsBuilder = new StringBuilder(" Friends: ");
			Node friendNode = user.getFriends().front;
			while (friendNode != null) {
				int friendId = (int) friendNode.element;
				User friend = findUserByID(friendId);
				if (friend != null) {
					friendsBuilder.append(friend.getName()).append(", ");
				}
				friendNode = friendNode.getNext();
			}
			if (friendsBuilder.toString().endsWith(", ")) {
				friendsBuilder.setLength(friendsBuilder.length() - 2);
			}
			if (friendsBuilder.length() == " Friends: ".length()) {
				friendsLabel.setText(" Friends: None");
			} else {
				friendsLabel.setText(friendsBuilder.toString());
			}

			StringBuilder ownPosts = new StringBuilder(" Posts Created: ");
			Node postNode = posts.front;
			while (postNode != null) {
				Post post = (Post) postNode.element;
				if (post.getCreatorID() == user.getID()) {
					ownPosts.append("Post ID ").append(post.getPostID()).append(" (\"").append(post.getContent())
							.append("\"), ");
				}
				postNode = postNode.getNext();
			}
			if (ownPosts.toString().endsWith(", ")) {
				ownPosts.setLength(ownPosts.length() - 2);
			}
			if (ownPosts.length() == " Posts Created: ".length()) {
				postsLabel.setText(" Posts Created: No posts");
			} else {
				postsLabel.setText(ownPosts.toString());
			}

			StringBuilder sharedPosts = new StringBuilder(" Posts Shared with Them: ");
			postNode = posts.front;
			while (postNode != null) {
				Post post = (Post) postNode.element;
				if (post.getSharedWith().contains(user.getID())) {
					sharedPosts.append("Post ID ").append(post.getPostID()).append(" (\"").append(post.getContent())
							.append("\"), ");
				}
				postNode = postNode.getNext();
			}
			if (sharedPosts.toString().endsWith(", ")) {
				sharedPosts.setLength(sharedPosts.length() - 2);
			}
			if (sharedPosts.length() == " Posts Shared with Them: ".length()) {
				sharedWithLabel.setText(" Posts Shared with Them: None");
			} else {
				sharedWithLabel.setText(sharedPosts.toString());
			}
		}
	}

	// Method to keep table data updated
	private void updateTable() {
		int start = currentPage * pageSize;
		int end = Math.min(start + pageSize, fullList.size());
		ObservableList<UserOverview> page = FXCollections.observableArrayList(fullList.subList(start, end));
		overviewTable.setItems(page);
	}

	// Method to return the observable list to be shown in table
	private ObservableList<UserOverview> getUserOverviewList() {
		ObservableList<UserOverview> list = FXCollections.observableArrayList();
		Node current = users.front;

		while (current != null) {
			User user = (User) current.element;

			// Convert friend IDs to friend names
			StringBuilder friendsBuilder = new StringBuilder();
			Node friendNode = user.getFriends().front;

			while (friendNode != null) {
				int friendID = (Integer) friendNode.element;
				User friend = findUserByID(friendID);
				if (friend != null) {
					friendsBuilder.append(friend.getName()).append(", ");
				}
				friendNode = friendNode.getNext();
			}

			String friendsStr;
			if (friendsBuilder.length() == 0) {
				friendsStr = "None";
			} else {
				friendsStr = friendsBuilder.substring(0, friendsBuilder.length() - 2);
			}

			// Collect posts by this user
			StringBuilder postsBuilder = new StringBuilder();
			Node postNode = posts.front;

			while (postNode != null) {
				Post post = (Post) postNode.element;

				if (post.getCreatorID() == user.getID()) {
					postsBuilder.append("[").append(post.getContent()).append(" on ").append(post.getDate())
							.append("]");
				}
				postNode = postNode.getNext();
			}

			String postsStr;
			if (postsBuilder.length() == 0) {
				postsStr = "No posts";
			} else {
				postsStr = postsBuilder.toString();
			}

			// Add to the observable list
			list.add(new UserOverview(user.getID(), user.getName(), user.getAge(), friendsStr, postsStr));
			current = current.getNext();
		}

		// Sort the list by ID in ascending order
		FXCollections.sort(list, (a, b) -> Integer.compare(a.getId(), b.getId()));

		return list;
	}

	// Method to open a dialog and add a user
	private void addUser() {
		Stage dialog = new Stage();
		dialog.setTitle("Add New User");

		Label idLabel = new Label("ID:");
		TextField idField = new TextField();

		Label nameLabel = new Label("Name:");
		TextField nameField = new TextField();

		Label ageLabel = new Label("Age:");
		TextField ageField = new TextField();

		Button addBtn = new Button("Add");
		addBtn.setOnAction(e -> {
			try {
				int id = Integer.parseInt(idField.getText().trim());
				String name = nameField.getText().trim();
				int age = Integer.parseInt(ageField.getText().trim());

				if (users.findUserByID(id) != null) {
					showAlert("Error", "User ID already exists.", Alert.AlertType.ERROR);
					return;
				}

				if (age < 18 || age >= 100) {
					showAlert("Invalid Age", "Age must be between 18 and 99.", Alert.AlertType.WARNING);
					return;
				}

				users.addUser(id, name, age);
				fullList = getUserOverviewList();
				updateTable();

				dialog.close();
				showAlert("Success", "User added successfully.", Alert.AlertType.INFORMATION);

			} catch (NumberFormatException ex) {
				showAlert("Invalid Input", "Please enter valid numeric values for ID and Age.", Alert.AlertType.ERROR);
			}
		});

		VBox layout = new VBox(10, idLabel, idField, nameLabel, nameField, ageLabel, ageField, addBtn);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));

		dialog.setScene(new Scene(layout, 300, 350));
		dialog.show();
	}

	// Method to open a dialog and update a user
	private void updateUser() {
		Stage dialog = new Stage();
		dialog.setTitle("Update User");

		Label IDLabel = new Label("Enter User ID:");
		TextField IDField = new TextField();

		Label nameLabel = new Label("New Name:");
		TextField nameField = new TextField();

		Label ageLabel = new Label("New Age:");
		TextField ageField = new TextField();

		Button updateBtn = new Button("Update");
		updateBtn.setOnAction(e -> {
			try {
				int ID = Integer.parseInt(IDField.getText().trim());
				String name = nameField.getText().trim();
				int age = Integer.parseInt(ageField.getText().trim());

				User user = users.findUserByID(ID);
				if (user == null) {
					showAlert("Not Found", "User with that ID does not exist.", Alert.AlertType.WARNING);
					return;
				}

				if (age < 18 || age >= 100) {
					showAlert("Invalid Age", "Age must be between 18 and 99.", Alert.AlertType.WARNING);
					return;
				}

				users.updateUser(ID, name, age);
				fullList = getUserOverviewList();
				updateTable();

				dialog.close();
				showAlert("Success", "User updated successfully.", Alert.AlertType.INFORMATION);

			} catch (NumberFormatException ex) {
				showAlert("Invalid Input", "Please enter valid numeric values for ID and Age.", Alert.AlertType.ERROR);
			}
		});

		VBox layout = new VBox(10, IDLabel, IDField, nameLabel, nameField, ageLabel, ageField, updateBtn);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));

		dialog.setScene(new Scene(layout, 300, 350));
		dialog.show();
	}

	// Method to open a dialog and delete a user
	private void deleteUser() {
		Stage dialog = new Stage();
		dialog.setTitle("Delete User");

		Label IDLabel = new Label("Enter User ID to Delete:");
		TextField IDField = new TextField();

		Button deleteBtn = new Button("Delete");
		deleteBtn.setOnAction(e -> {
			try {
				int ID = Integer.parseInt(IDField.getText().trim());

				User user = users.findUserByID(ID);
				if (user == null) {
					showAlert("Not Found", "User with that ID does not exist.", Alert.AlertType.WARNING);
					return;
				}

				// Confirm deletion
				Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
				confirm.setTitle("Confirm Deletion");
				confirm.setHeaderText(null);
				confirm.setContentText("Are you sure you want to delete user " + user.getName() + "?");

				Optional<ButtonType> result = confirm.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					users.deleteUserByID(ID);
					removeUserReferences(ID);
					fullList = getUserOverviewList();
					updateTable();
					showAlert("Success", "User deleted successfully.", Alert.AlertType.INFORMATION);
					dialog.close();
				}

			} catch (NumberFormatException ex) {
				showAlert("Invalid Input", "Please enter a valid numeric ID.", Alert.AlertType.ERROR);
			}
		});

		VBox layout = new VBox(10, IDLabel, IDField, deleteBtn);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));
		dialog.setScene(new Scene(layout, 300, 200));
		dialog.show();
	}

	// Method to remove all user's data from the system
	private void removeUserReferences(int userID) {
		// Remove posts created by the user
		Node currentPost = posts.front;
		Node prevPost = null;

		while (currentPost != null) {
			Post post = (Post) currentPost.element;

			if (post.getCreatorID() == userID) {
				// Remove the node
				if (prevPost == null) {
					// Removing from the front
					posts.front = currentPost.next;
				} else {
					prevPost.next = currentPost.next;
				}
				if (currentPost == posts.back) {
					posts.back = prevPost;
				}

				posts.size--;

				// Move to the next node after deletion
				if (prevPost == null) {
					currentPost = posts.front;
				} else {
					currentPost = prevPost.next;
				}
				continue;
			} else {
				// Remove userID from sharedWith list
				post.getSharedWith().remove(userID);
			}

			// Move forward
			prevPost = currentPost;
			currentPost = currentPost.next;
		}

		// Remove userID from all users friends lists
		Node userNode = users.front;
		while (userNode != null) {
			User user = (User) userNode.element;
			user.getFriends().remove(userID);
			userNode = userNode.next;
		}
	}

	// Method to open a dialog and search for a user
	private void searchUser() {
		Stage dialog = new Stage();
		dialog.setTitle("Search User");

		Label typeLabel = new Label("Search by:");
		ComboBox<String> searchTypeCombo = new ComboBox<>();
		searchTypeCombo.getItems().addAll("ID", "Name");
		searchTypeCombo.setValue("ID");

		Label inputLabel = new Label("Enter value:");
		TextField inputField = new TextField();

		Button searchBtn = new Button("Search");

		searchBtn.setOnAction(e -> {
			String input = inputField.getText().trim();
			String searchType = searchTypeCombo.getValue();

			if (input.isEmpty()) {
				showAlert("Input Required", "Please enter a value to search.", Alert.AlertType.WARNING);
				return;
			}

			if (searchType.equals("ID")) {
				try {
					int ID = Integer.parseInt(input);
					User foundUser = users.findUserByID(ID);

					if (foundUser != null) {
						showAlert("User Found", "User: " + foundUser.getName() + "\nID: " + foundUser.getID()
								+ "\nAge: " + foundUser.getAge(), Alert.AlertType.INFORMATION);
					} else {
						showAlert("User Not Found", "No user found with ID: " + input, Alert.AlertType.ERROR);
					}
				} catch (NumberFormatException ex) {
					showAlert("Invalid ID", "Please enter a valid numeric ID.", Alert.AlertType.ERROR);
				}
			} else { // Search by name
				LinkedList matches = new LinkedList();
				Node current = users.front;

				while (current != null) {
					if (current.element instanceof User) {
						User user = (User) current.element;
						if (user.getName().equalsIgnoreCase(input)) {
							matches.addLast(user);
						}
					}
					current = current.next;
				}

				if (matches.size() > 0) {
					StringBuilder result = new StringBuilder("Users Found:\n");
					Node node = matches.front;

					while (node != null) {
						User user = (User) node.element;
						result.append("Name: ").append(user.getName()).append(", ID: ").append(user.getID())
								.append(", Age: ").append(user.getAge()).append("\n");
						node = node.next;
					}

					showAlert("User(s) Found", result.toString(), Alert.AlertType.INFORMATION);
				} else {
					showAlert("User Not Found", "No users found with name: " + input, Alert.AlertType.ERROR);
				}
			}
		});

		VBox layout = new VBox(10, typeLabel, searchTypeCombo, inputLabel, inputField, searchBtn);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));

		dialog.setScene(new Scene(layout, 300, 250));
		dialog.show();
	}

	// Method to open a dialog to add or delete friends for a user
	private void manageFriends() {
		Stage dialog = new Stage();
		dialog.setTitle("Manage Friends");

		Label userIDLabel = new Label("User ID:");
		TextField userIDField = new TextField();

		Label friendIDLabel = new Label("Friend ID:");
		TextField friendIDField = new TextField();

		Button addBtn = new Button("Add Friend");
		Button removeBtn = new Button("Remove Friend");

		addBtn.setOnAction(e -> {
			try {
				int userID = Integer.parseInt(userIDField.getText().trim());
				int friendID = Integer.parseInt(friendIDField.getText().trim());

				User user = users.findUserByID(userID);
				User friend = users.findUserByID(friendID);

				if (user == null || friend == null) {
					showAlert("Error", "User or Friend not found.", Alert.AlertType.ERROR);
					return;
				}

				if (user.getFriends().contains(friendID)) {
					showAlert("Info", "They are already friends.", Alert.AlertType.INFORMATION);
					return;
				}

				// Add friendship between these two users
				user.addFriend(friendID);
				friend.addFriend(userID);

				showAlert("Success", "Friend added successfully.", Alert.AlertType.INFORMATION);
				fullList = getUserOverviewList();
				updateTable();

			} catch (NumberFormatException ex) {
				showAlert("Invalid Input", "Please enter valid numeric IDs.", Alert.AlertType.ERROR);
			}
		});

		removeBtn.setOnAction(e -> {
			try {
				int userID = Integer.parseInt(userIDField.getText().trim());
				int friendID = Integer.parseInt(friendIDField.getText().trim());

				User user = users.findUserByID(userID);
				User friend = users.findUserByID(friendID);

				if (user == null || friend == null) {
					showAlert("Error", "User or Friend not found.", Alert.AlertType.ERROR);
					return;
				}

				if (!user.getFriends().contains(friendID)) {
					showAlert("Info", "Friend ID not found in user's friend list.", Alert.AlertType.INFORMATION);
					return;
				}

				// Remove friendship between these two users
				user.getFriends().remove(friendID);
				friend.getFriends().remove(userID);

				showAlert("Success", "Friend removed successfully.", Alert.AlertType.INFORMATION);
				fullList = getUserOverviewList();
				updateTable();

			} catch (NumberFormatException ex) {
				showAlert("Invalid Input", "Please enter valid numeric IDs.", Alert.AlertType.ERROR);
			}
		});

		HBox buttonsBox = new HBox(10, addBtn, removeBtn);
		buttonsBox.setAlignment(Pos.CENTER);

		VBox layout = new VBox(10, userIDLabel, userIDField, friendIDLabel, friendIDField, buttonsBox);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));

		Scene scene = new Scene(layout, 300, 250);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to open a dialog to add a post and share it
	private void addPost() {
		Stage dialog = new Stage();
		dialog.setTitle("Create Post");

		Label posterLabel = new Label("Poster ID:");
		TextField posterField = new TextField();

		Label contentLabel = new Label("Post Content:");
		TextField contentField = new TextField();

		Label dateLabel = new Label("Choose Date:");
		DatePicker datePicker = new DatePicker();

		Label shareWithLabel = new Label("Share with (comma-separated IDs or 'All'):");
		TextField shareField = new TextField();

		Button createPostBtn = new Button("Create Post");
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red;");

		createPostBtn.setOnAction(e -> {
			String posterIDText = posterField.getText().trim();
			String content = contentField.getText().trim();
			String shareInput = shareField.getText().trim();

			if (posterIDText.isEmpty() || content.isEmpty() || datePicker.getValue() == null) {
				errorLabel.setText("Poster ID, content, and date are required.");
				return;
			}

			LocalDate selectedDate = datePicker.getValue();
			if (selectedDate.isAfter(LocalDate.now())) {
				errorLabel.setText("Date cannot be in the future.");
				return;
			}

			try {
				int posterID = Integer.parseInt(posterIDText);
				User poster = (User) users.findUserByID(posterID);
				if (poster == null) {
					errorLabel.setText("User with ID " + posterID + " not found.");
					return;
				}

				// Format the date
				String formattedDate = formatDate(datePicker.getValue());
				createNewPost(posterID, content, shareInput, poster, formattedDate);
				dialog.close();

				fullList = getUserOverviewList();
				updateTable();

			} catch (NumberFormatException ex) {
				errorLabel.setText("Poster ID must be a number.");
			}
		});

		VBox layout = new VBox(10, posterLabel, posterField, contentLabel, contentField, dateLabel, datePicker,
				shareWithLabel, shareField, createPostBtn, errorLabel);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));

		Scene dialogScene = new Scene(layout, 400, 400);
		dialog.setScene(dialogScene);
		dialog.show();
	}

	// Method to format the date to DD.MM.YY
	private String formatDate(LocalDate date) {
		int day = date.getDayOfMonth();
		int month = date.getMonthValue();
		int year = date.getYear();

		return String.format("%02d.%02d.%d", day, month, year);
	}

	// Helper method to add a new post to the system
	private void createNewPost(int posterID, String content, String shareInput, User poster, String date) {
		int newPostID = posts.size() + 1;
		Post newPost = new Post(newPostID, posterID, content, date);

		if (shareInput.equalsIgnoreCase("All")) {
			Node friendNode = poster.getFriends().front;
			while (friendNode != null) {
				int friendID = (Integer) friendNode.element;
				newPost.addSharedUser(friendID);
				friendNode = friendNode.next;
			}
		} else {
			String[] IDStrings = shareInput.split(",");
			for (int i = 0; i < IDStrings.length; i++) {
				String idStr = IDStrings[i].trim();
				try {
					int friendID = Integer.parseInt(idStr);
					if (poster.getFriends().contains(friendID)) {
						newPost.addSharedUser(friendID);
					}
				} catch (NumberFormatException ex) {
				}
			}
		}

		posts.addLast(newPost);
	}

	// Method to show posts shared with a specific user
	private void showSharedPosts() {
		Stage dialog = new Stage();
		dialog.setTitle("Posts Shared With User");

		Label titleLabel = new Label("View Posts Shared With:");
		ComboBox<String> userComboBox = new ComboBox<>();

		// Populate userComboBox with all users' names
		Node node = users.front;
		while (node != null) {
			if (node.element instanceof User) {
				User user = (User) node.element;
				userComboBox.getItems().add(user.getName());
			}
			node = node.next;
		}

		TextArea resultArea = new TextArea();
		resultArea.setEditable(false);
		resultArea.setWrapText(true);
		resultArea.setPrefHeight(300);

		Button showBtn = new Button("Show Shared Posts");
		showBtn.setOnAction(e -> {
			String selectedName = userComboBox.getValue();
			if (selectedName == null || selectedName.isEmpty()) {
				resultArea.setText("Please select a user.");
				return;
			}

			User selectedUser = users.findUserByName(selectedName);
			if (selectedUser == null) {
				resultArea.setText("User not found.");
				return;
			}

			StringBuilder sb = new StringBuilder("Posts shared with " + selectedName + ":\n\n");
			Node postNode = posts.front;
			boolean hasPosts = false;

			while (postNode != null) {
				if (postNode.element instanceof Post) {
					Post post = (Post) postNode.element;
					if (post.getSharedWith().contains(selectedUser.getID())) {
						User creator = users.findUserByID(post.getCreatorID());
						sb.append("From: ").append(creator.getName()).append(" | Date: ").append(post.getDate())
								.append("\n").append(post.getContent()).append("\n\n");
						hasPosts = true;
					}
				}
				postNode = postNode.next;
			}

			if (!hasPosts) {
				sb.append("No posts shared with this user.");
			}

			resultArea.setText(sb.toString());
		});

		VBox layout = new VBox(10, titleLabel, userComboBox, showBtn, resultArea);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 500, 400);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to delete a post from the system
	private void deletePosts() {
		Stage dialog = new Stage();
		dialog.setTitle("Delete Posts");

		Label titleLabel = new Label("Select User:");
		ComboBox<String> userComboBox = new ComboBox<>();

		// Populate userComboBox with all users
		Node userNode = users.front;
		while (userNode != null) {
			if (userNode.element instanceof User) {
				User user = (User) userNode.element;
				userComboBox.getItems().add(user.getName());
			}
			userNode = userNode.next;
		}

		Label postIDLabel = new Label("Enter Post ID to delete:");
		TextField postIDField = new TextField();

		Button deleteBtn = new Button("Delete Post");

		deleteBtn.setOnAction(e -> {
			String selectedUserName = userComboBox.getValue();
			String postIDText = postIDField.getText().trim();

			if (selectedUserName == null || selectedUserName.isEmpty() || postIDText.isEmpty()) {
				showAlert("Input Error", "Please select a user and enter a post ID.", Alert.AlertType.ERROR);
				return;
			}

			int postID;
			try {
				postID = Integer.parseInt(postIDText);
			} catch (NumberFormatException ex) {
				showAlert("Input Error", "Invalid post ID.", Alert.AlertType.ERROR);
				return;
			}

			User selectedUser = users.findUserByName(selectedUserName);
			if (selectedUser == null) {
				showAlert("User Not Found", "User not found.", Alert.AlertType.ERROR);
				return;
			}

			Node postNode = posts.front;
			Node prev = null;

			while (postNode != null) {
				if (postNode.element instanceof Post) {
					Post post = (Post) postNode.element;
					if (post.getPostID() == postID) {
						if (post.getCreatorID() == selectedUser.getID()) {
							// Delete node from LinkedList
							if (prev == null) {
								posts.front = postNode.next;
							} else {
								prev.next = postNode.next;
							}
							if (postNode == posts.back) {
								posts.back = prev;
							}
							posts.size--;
							showAlert("Success", "Post successfully deleted from the system.",
									Alert.AlertType.INFORMATION);
							fullList = getUserOverviewList();
							updateTable();
						} else {
							showAlert("Authorization Error", "You can only delete your own posts.",
									Alert.AlertType.ERROR);
						}
						return;
					}
				}
				prev = postNode;
				postNode = postNode.next;
			}

			showAlert("Post Not Found", "Post with ID " + postID + " not found.", Alert.AlertType.ERROR);
		});

		VBox layout = new VBox(10, titleLabel, userComboBox, postIDLabel, postIDField, deleteBtn);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 450, 400);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to remove a shared post from a user's view
	private void removeSharedPost() {
		Stage dialog = new Stage();
		dialog.setTitle("Remove Shared Post View");

		Label titleLabel = new Label("Select User:");
		ComboBox<String> userComboBox = new ComboBox<>();

		// Populate userComboBox with all users
		Node userNode = users.front;
		while (userNode != null) {
			if (userNode.element instanceof User) {
				User user = (User) userNode.element;
				userComboBox.getItems().add(user.getName());
			}
			userNode = userNode.next;
		}

		Label postIDLabel = new Label("Enter Post ID to remove from view:");
		TextField postIDField = new TextField();

		Button removeBtn = new Button("Remove from View");

		removeBtn.setOnAction(e -> {
			String selectedUserName = userComboBox.getValue();
			String postIDText = postIDField.getText().trim();

			if (selectedUserName == null || selectedUserName.isEmpty() || postIDText.isEmpty()) {
				showAlert("Input Error", "Please select a user and enter a post ID.", Alert.AlertType.ERROR);
				return;
			}

			int postID;
			try {
				postID = Integer.parseInt(postIDText);
			} catch (NumberFormatException ex) {
				showAlert("Input Error", "Invalid post ID.", Alert.AlertType.ERROR);
				return;
			}

			User selectedUser = users.findUserByName(selectedUserName);
			if (selectedUser == null) {
				showAlert("User Not Found", "User not found.", Alert.AlertType.ERROR);
				return;
			}

			Node postNode = posts.front;
			while (postNode != null) {
				if (postNode.element instanceof Post) {
					Post post = (Post) postNode.element;
					if (post.getPostID() == postID) {
						boolean removed = post.removeSharedUser(selectedUser.getID());
						if (removed) {
							showAlert("Success", "Post removed from " + selectedUserName + "'s view.",
									Alert.AlertType.INFORMATION);
						} else {
							showAlert("Not Shared", "This post is not shared with " + selectedUserName + ".",
									Alert.AlertType.WARNING);
						}
						return;
					}
				}
				postNode = postNode.next;
			}

			showAlert("Post Not Found", "Post with ID " + postID + " not found.", Alert.AlertType.ERROR);
		});

		VBox layout = new VBox(10, titleLabel, userComboBox, postIDLabel, postIDField, removeBtn);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 450, 400);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to show all posts of a specific user
	private void showUserPosts() {
		Stage dialog = new Stage();
		dialog.setTitle("User's Posts");

		Label selectUserLabel = new Label("Select User:");
		ComboBox<String> userComboBox = new ComboBox<>();

		// Populate combo box with user names
		Node userNode = users.front;
		while (userNode != null) {
			if (userNode.element instanceof User) {
				User user = (User) userNode.element;
				userComboBox.getItems().add(user.getName());
			}
			userNode = userNode.next;
		}

		Button showBtn = new Button("Show Posts");
		TextArea postsArea = new TextArea();
		postsArea.setEditable(false);
		postsArea.setWrapText(true);
		postsArea.setPrefHeight(300);

		showBtn.setOnAction(e -> {
			String selectedUserName = userComboBox.getValue();
			if (selectedUserName == null || selectedUserName.isEmpty()) {
				showAlert("Input Error", "Please select a user.", Alert.AlertType.ERROR);
				return;
			}

			User selectedUser = users.findUserByName(selectedUserName);
			if (selectedUser == null) {
				showAlert("User Not Found", "User not found.", Alert.AlertType.ERROR);
				return;
			}

			StringBuilder result = new StringBuilder();
			Node postNode = posts.front;
			boolean found = false;

			while (postNode != null) {
				if (postNode.element instanceof Post) {
					Post post = (Post) postNode.element;
					if (post.getCreatorID() == selectedUser.getID()) {
						found = true;

						// Build a list of names from sharedWith IDs
						StringBuilder sharedNames = new StringBuilder();
						Node sharedNode = post.getSharedWith().front;
						while (sharedNode != null) {
							if (sharedNode.element instanceof Integer) {
								int sharedID = (Integer) sharedNode.element;
								User sharedUser = users.findUserByID(sharedID);
								if (sharedUser != null) {
									if (sharedNames.length() > 0) {
										sharedNames.append(", ");
									}
									sharedNames.append(sharedUser.getName());
								}
							}
							sharedNode = sharedNode.next;
						}

						result.append("Post ID: ").append(post.getPostID()).append("\n").append("Content: ")
								.append(post.getContent()).append("\n").append("Date: ").append(post.getDate())
								.append("\n").append("Shared With: ").append(sharedNames.toString()).append("\n\n");
					}
				}
				postNode = postNode.next;
			}

			if (!found) {
				result.append("No posts found for ").append(selectedUserName).append(".");
			}

			postsArea.setText(result.toString());
		});

		VBox layout = new VBox(10, selectUserLabel, userComboBox, showBtn, postsArea);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 500, 450);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to show posts shared with a specific user
	private void showPostsSharedWithUser() {
		Stage dialog = new Stage();
		dialog.setTitle("Posts Shared With a User");

		Label selectUserLabel = new Label("Select User:");
		ComboBox<String> userComboBox = new ComboBox<>();

		// Populate the ComboBox with user names
		Node userNode = users.front;
		while (userNode != null) {
			if (userNode.element instanceof User) {
				User user = (User) userNode.element;
				userComboBox.getItems().add(user.getName());
			}
			userNode = userNode.next;
		}

		Button showBtn = new Button("Show Posts");
		TextArea postsArea = new TextArea();
		postsArea.setEditable(false);
		postsArea.setWrapText(true);
		postsArea.setPrefHeight(300);

		showBtn.setOnAction(e -> {
			String selectedUserName = userComboBox.getValue();
			if (selectedUserName == null || selectedUserName.isEmpty()) {
				showAlert("Input Error", "Please select a user.", Alert.AlertType.ERROR);
				return;
			}

			User selectedUser = users.findUserByName(selectedUserName);
			if (selectedUser == null) {
				showAlert("User Not Found", "User not found.", Alert.AlertType.ERROR);
				return;
			}

			StringBuilder result = new StringBuilder();
			Node postNode = posts.front;
			boolean found = false;

			while (postNode != null) {
				if (postNode.element instanceof Post) {
					Post post = (Post) postNode.element;
					Node sharedNode = post.getSharedWith().front;

					while (sharedNode != null) {
						if (sharedNode.element instanceof Integer
								&& (Integer) sharedNode.element == selectedUser.getID()) {
							User poster = users.findUserByID(post.getCreatorID());
							String posterName = "Unknown";
							if (poster != null) {
								posterName = poster.getName();
							}

							found = true;
							result.append("Post ID: ").append(post.getPostID()).append("\n").append("From: ")
									.append(posterName).append("\n").append("Content: ").append(post.getContent())
									.append("\n").append("Date: ").append(post.getDate()).append("\n\n");
							break;
						}
						sharedNode = sharedNode.next;
					}
				}
				postNode = postNode.next;
			}

			if (!found) {
				result.append("No posts shared with ").append(selectedUserName).append(".");
			}

			postsArea.setText(result.toString());
		});

		VBox layout = new VBox(10, selectUserLabel, userComboBox, showBtn, postsArea);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 500, 450);
		dialog.setScene(scene);
		dialog.show();
	}

	private void showMostActiveUsers() {
		Stage dialog = new Stage();
		dialog.setTitle("Most Active Users");

		Label topNLabel = new Label("Enter number of top active users to display:");
		TextField topNField = new TextField();
		Button showBtn = new Button("Show");
		TextArea resultArea = new TextArea();
		resultArea.setEditable(false);
		resultArea.setWrapText(true);
		resultArea.setPrefHeight(300);

		showBtn.setOnAction(e -> {
			String input = topNField.getText().trim();
			if (input.isEmpty()) {
				showAlert("Input Error", "Please enter a number.", Alert.AlertType.ERROR);
				return;
			}

			int topN;
			try {
				topN = Integer.parseInt(input);
			} catch (NumberFormatException ex) {
				showAlert("Input Error", "Invalid number format.", Alert.AlertType.ERROR);
				return;
			}

			LinkedList userIDs = new LinkedList();
			LinkedList postCounts = new LinkedList();

			// Count posts per user
			Node userNode = users.front;
			while (userNode != null) {
				if (userNode.element instanceof User) {
					User user = (User) userNode.element;
					int count = 0;

					Node postNode = posts.front;
					while (postNode != null) {
						if (postNode.element instanceof Post) {
							Post post = (Post) postNode.element;
							if (post.getCreatorID() == user.getID()) {
								count++;
							}
						}
						postNode = postNode.next;
					}

					userIDs.addLast(user.getID());
					postCounts.addLast(count);
				}
				userNode = userNode.next;
			}

			// Display top N users with highest counts
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < topN; i++) {
				Node currentID = userIDs.front;
				Node currentCount = postCounts.front;
				Node maxIDNode = currentID;
				Node maxCountNode = currentCount;
				Node prevMaxID = null;
				Node prevMaxCount = null;
				Node prevID = null;
				Node prevCount = null;

				while (currentID != null && currentCount != null) {
					if ((int) currentCount.element > (int) maxCountNode.element) {
						maxIDNode = currentID;
						maxCountNode = currentCount;
						prevMaxID = prevID;
						prevMaxCount = prevCount;
					}
					prevID = currentID;
					prevCount = currentCount;
					currentID = currentID.next;
					currentCount = currentCount.next;
				}

				if (maxIDNode == null || maxCountNode == null) {
					break;
				}

				int userID = (int) maxIDNode.element;
				int postCount = (int) maxCountNode.element;
				User user = users.findUserByID(userID);
				if (user != null) {
					result.append((i + 1)).append(". ").append(user.getName()).append(" - ").append(postCount)
							.append(" posts\n");
				}

				// Remove max so it doesn't get selected again
				userIDs.deleteUserByID(userID);
				postCounts.remove(postCount);
			}

			if (result.length() == 0) {
				result.append("No users found.");
			}

			resultArea.setText(result.toString());
		});

		VBox layout = new VBox(10, topNLabel, topNField, showBtn, resultArea);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 500, 400);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to show post status of each user (How many posts created and shared
	// with him/her)
	private void showEngagementMetrics() {
		Stage dialog = new Stage();
		dialog.setTitle("User Engagement Metrics");

		TextArea resultArea = new TextArea();
		resultArea.setEditable(false);
		resultArea.setWrapText(true);
		resultArea.setPrefHeight(400);

		StringBuilder result = new StringBuilder();

		Node userNode = users.front;
		while (userNode != null) {
			if (userNode.element instanceof User) {
				User user = (User) userNode.element;
				int createdCount = 0;
				int sharedWithCount = 0;

				Node postNode = posts.front;
				while (postNode != null) {
					if (postNode.element instanceof Post) {
						Post post = (Post) postNode.element;

						// Count posts created by this user
						if (post.getCreatorID() == user.getID()) {
							createdCount++;
						}

						// Count posts shared with this user
						LinkedList sharedList = post.getSharedWith();
						Node sharedNode = sharedList.front;
						while (sharedNode != null) {
							if (sharedNode.element instanceof Integer) {
								int sharedID = (int) sharedNode.element;
								if (sharedID == user.getID()) {
									sharedWithCount++;
									break;
								}
							}
							sharedNode = sharedNode.next;
						}
					}
					postNode = postNode.next;
				}

				result.append(user.getName()).append(" has created ").append(createdCount).append(" ");
				if (createdCount == 1) {
					result.append("post");
				} else {
					result.append("posts");
				}
				result.append(" and has ").append(sharedWithCount).append(" ");
				if (sharedWithCount == 1) {
					result.append("post shared");
				} else {
					result.append("posts shared");
				}
				result.append(" with them.\n\n");
			}
			userNode = userNode.next;
		}

		if (result.length() == 0) {
			result.append("No users found.");
		}

		resultArea.setText(result.toString());

		VBox layout = new VBox(10, resultArea);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 500, 450);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to show all friends of a specific user, in ascending or descending
	private void showUserFriends() {
		Stage dialog = new Stage();
		dialog.setTitle("View User's Friends");

		Label userLabel = new Label("Select User:");
		ComboBox<String> userComboBox = new ComboBox<>();

		// Populate users
		Node userNode = users.front;
		while (userNode != null) {
			if (userNode.element instanceof User) {
				User user = (User) userNode.element;
				userComboBox.getItems().add(user.getName());
			}
			userNode = userNode.next;
		}

		Label sortLabel = new Label("Sort Order:");
		ComboBox<String> sortComboBox = new ComboBox<>();
		sortComboBox.getItems().addAll("Ascending", "Descending");
		sortComboBox.setValue("Ascending");

		Button viewBtn = new Button("Show Friends");
		TextArea resultArea = new TextArea();
		resultArea.setEditable(false);
		resultArea.setWrapText(true);
		resultArea.setPrefHeight(300);

		viewBtn.setOnAction(e -> {
			String selectedUserName = userComboBox.getValue();
			String sortOrder = sortComboBox.getValue();

			if (selectedUserName == null || selectedUserName.isEmpty()) {
				showAlert("Input Error", "Please select a user.", Alert.AlertType.ERROR);
				return;
			}

			User selectedUser = users.findUserByName(selectedUserName);
			if (selectedUser == null) {
				showAlert("User Not Found", "User not found.", Alert.AlertType.ERROR);
				return;
			}

			LinkedList friendIDs = selectedUser.getFriends();
			if (friendIDs == null || friendIDs.size() == 0) {
				resultArea.setText(selectedUser.getName() + " has no friends.");
				return;
			}

			// Convert IDs to names using LinkedList
			LinkedList friendNames = new LinkedList();
			Node friendNode = friendIDs.front;
			while (friendNode != null) {
				if (friendNode.element instanceof Integer) {
					int friendID = (int) friendNode.element;
					User friendUser = users.findUserByID(friendID);
					if (friendUser != null) {
						friendNames.addLast(friendUser.getName());
					}
				}
				friendNode = friendNode.next;
			}

			// Sort the friend names
			sortLinkedList(friendNames, sortOrder.equals("Ascending"));

			// Prepare result
			StringBuilder result = new StringBuilder("Friends of " + selectedUserName + ":\n\n");
			Node nameNode = friendNames.front;
			while (nameNode != null) {
				result.append("- ").append(nameNode.element.toString()).append("\n");
				nameNode = nameNode.next;
			}

			resultArea.setText(result.toString());
		});

		VBox layout = new VBox(10, userLabel, userComboBox, sortLabel, sortComboBox, viewBtn, resultArea);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 450, 450);
		dialog.setScene(scene);
		dialog.show();
	}

	// Helper method to sort friends ascendingly
	private void sortLinkedList(LinkedList list, boolean ascending) {
		if (list == null || list.size < 2)
			return;

		boolean swapped;
		do {
			swapped = false;
			Node current = list.front;
			while (current != null && current.next != null) {
				String a = current.element.toString();
				String b = current.next.element.toString();
				if ((ascending && a.compareToIgnoreCase(b) > 0) || (!ascending && a.compareToIgnoreCase(b) < 0)) {
					// Swap elements
					Object temp = current.element;
					current.element = current.next.element;
					current.next.element = temp;
					swapped = true;
				}
				current = current.next;
			}
		} while (swapped);
	}

	// Save all posts to a new file, in unsorted, ascending or descending
	private void savePostsToFile() {
		Stage dialog = new Stage();
		dialog.setTitle("Save Posts To File");

		Label sortLabel = new Label("Sort by Username:");
		ComboBox<String> sortOrderBox = new ComboBox<>();
		sortOrderBox.getItems().addAll("Unsorted", "Ascending", "Descending");
		sortOrderBox.setValue("Unsorted");

		Button saveBtn = new Button("Save");

		saveBtn.setOnAction(event -> {
			String selectedOrder = sortOrderBox.getValue();

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Posts To File");
			fileChooser.setInitialFileName("updated_posts.txt");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
			File file = fileChooser.showSaveDialog(dialog);

			if (file != null) {
				try (FileWriter writer = new FileWriter(file)) {

					writer.write("Posts Created Report\n");
					writer.write("--------------------\n\n");

					LinkedList tempUsers = new LinkedList();
					Node userNode = users.front;
					while (userNode != null) {
						if (userNode.element instanceof User) {
							User user = (User) userNode.element;
							tempUsers.addLast(user);
						}
						userNode = userNode.next;
					}

					if (selectedOrder.equals("Ascending")) {
						sortUsersByName(tempUsers, true);
					} else if (selectedOrder.equals("Descending")) {
						sortUsersByName(tempUsers, false);
					}

					Node sortedUserNode = tempUsers.front;
					while (sortedUserNode != null) {
						if (sortedUserNode.element instanceof User) {
							User user = (User) sortedUserNode.element;
							int userId = user.getID();
							String userName = user.getName();

							Node postNode = posts.front;
							boolean hasPosts = false;
							StringBuilder userSection = new StringBuilder();

							while (postNode != null) {
								if (postNode.element instanceof Post) {
									Post post = (Post) postNode.element;

									if (post.getCreatorID() == userId) {
										if (!hasPosts) {
											userSection.append("User: ").append(userName).append("\n");
											hasPosts = true;
										}

										userSection.append("- Post ID: ").append(post.getPostID()).append(", Content: ")
												.append(post.getContent()).append(", ").append(post.getDate());

										LinkedList shared = post.getSharedWith();
										if (shared != null && shared.size() > 0) {
											userSection.append(", Shared With: ");
											Node sharedNode = shared.front;
											boolean first = true;
											while (sharedNode != null) {
												if (sharedNode.element instanceof Integer) {
													int sharedID = (int) sharedNode.element;
													User sharedUser = users.findUserByID(sharedID);
													if (sharedUser != null) {
														if (!first) {
															userSection.append(", ");
														}
														userSection.append(sharedUser.getName());
														first = false;
													}
												}
												sharedNode = sharedNode.next;
											}
										}

										userSection.append("\n");
									}
								}
								postNode = postNode.next;
							}

							if (hasPosts) {
								writer.write(userSection.toString());
								writer.write("\n");
							}
						}
						sortedUserNode = sortedUserNode.next;
					}

					showAlert("Success", "Posts saved successfully to updated_posts.txt", Alert.AlertType.INFORMATION);

				} catch (IOException ex) {
					showAlert("Error", "An error occurred while saving posts: " + ex.getMessage(),
							Alert.AlertType.ERROR);
				}
			}
		});

		VBox layout = new VBox(15, sortLabel, sortOrderBox, saveBtn);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 300, 180);
		dialog.setScene(scene);
		dialog.show();
	}

	// Method to save all shared posts in the system to a new file
	private void saveSharedPostToFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Posts Shared With Users");
		fileChooser.setInitialFileName("posts_shared.txt");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
		File file = fileChooser.showSaveDialog(null);

		if (file != null) {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write("Posts Shared With User Report\n");
				writer.write("-----------------------------\n\n");

				Node userNode = users.front;
				while (userNode != null) {
					if (userNode.element instanceof User) {
						User user = (User) userNode.element;
						int userId = user.getID();
						String userName = user.getName();

						Node postNode = posts.front;
						boolean hasSharedPosts = false;
						StringBuilder userSection = new StringBuilder();

						while (postNode != null) {
							if (postNode.element instanceof Post) {
								Post post = (Post) postNode.element;
								LinkedList shared = post.getSharedWith();

								if (shared != null && shared.contains(userId)) {
									if (!hasSharedPosts) {
										userSection.append("User: ").append(userName).append("\n");
										hasSharedPosts = true;
									}

									User creator = users.findUserByID(post.getCreatorID());
									String creatorName = creator != null ? creator.getName() : "Unknown";

									userSection.append("- Post ID: ").append(post.getPostID()).append(", Content: ")
											.append(post.getContent()).append(", ").append(post.getDate())
											.append(", Creator: ").append(creatorName).append("\n");
								}
							}
							postNode = postNode.next;
						}

						if (hasSharedPosts) {
							writer.write(userSection.toString());
							writer.write("\n");
						}
					}
					userNode = userNode.next;
				}

				showAlert("Success", "Posts shared with users saved to posts_shared.txt", Alert.AlertType.INFORMATION);

			} catch (IOException ex) {
				showAlert("Error", "Failed to save posts: " + ex.getMessage(), Alert.AlertType.ERROR);
			}
		}
	}

	// Helper method to sort users by their names
	private void sortUsersByName(LinkedList list, boolean ascending) {
		if (list.size() < 2)
			return;

		boolean swapped;

		do {
			swapped = false;
			Node current = list.front;

			while (current != null && current.next != null) {
				if (current.element instanceof User && current.next.element instanceof User) {
					User user1 = (User) current.element;
					User user2 = (User) current.next.element;

					boolean shouldSwap = false;

					if (ascending) {
						if (user1.getName().compareToIgnoreCase(user2.getName()) > 0) {
							shouldSwap = true;
						}
					} else {
						if (user1.getName().compareToIgnoreCase(user2.getName()) < 0) {
							shouldSwap = true;
						}
					}

					if (shouldSwap) {
						Object temp = current.element;
						current.element = current.next.element;
						current.next.element = temp;
						swapped = true;
					}
				}
				current = current.next;
			}
		} while (swapped);
	}

	// Method to save all updated data to new files to the Desktop
	private void saveAllData() {
		try {
			String Path = System.getProperty("user.home") + File.separator + "Desktop";

			// Save users
			PrintWriter userWriter = new PrintWriter(Path + File.separator + "updated_users.txt");
			userWriter.println("User ID,Name,Age");
			Node userNode = users.front;
			while (userNode != null) {
				if (userNode.element instanceof User) {
					User user = (User) userNode.element;
					userWriter.println(user.getID() + "," + user.getName() + "," + user.getAge());
				}
				userNode = userNode.next;
			}
			userWriter.close();

			// Save friendships
			PrintWriter friendWriter = new PrintWriter(Path + File.separator + "updated_friendships.txt");
			friendWriter.println("User ID,Friends");
			userNode = users.front;
			while (userNode != null) {
				if (userNode.element instanceof User) {
					User user = (User) userNode.element;
					StringBuilder line = new StringBuilder();
					line.append(user.getID());
					LinkedList friendsList = user.getFriends();
					Node friendNode = friendsList.front;
					while (friendNode != null) {
						if (friendNode.element instanceof Integer) {
							line.append(",").append(friendNode.element);
						}
						friendNode = friendNode.next;
					}
					friendWriter.println(line.toString());
				}
				userNode = userNode.next;
			}
			friendWriter.close();

			// Save posts
			PrintWriter postWriter = new PrintWriter(Path + File.separator + "updated_posts.txt");
			postWriter.println("Post ID,Creator ID,Content,Creation Date,Shared With");
			Node postNode = posts.front;
			while (postNode != null) {
				if (postNode.element instanceof Post) {
					Post post = (Post) postNode.element;
					StringBuilder line = new StringBuilder();
					line.append(post.getPostID()).append(",");
					line.append(post.getCreatorID()).append(",");
					line.append(post.getContent().replace(",", " ")).append(",");
					line.append(post.getDate());
					LinkedList sharedWithList = post.getSharedWith();
					Node sharedNode = sharedWithList.front;
					while (sharedNode != null) {
						if (sharedNode.element instanceof Integer) {
							line.append(",").append(sharedNode.element);
						}
						sharedNode = sharedNode.next;
					}
					postWriter.println(line.toString());
				}
				postNode = postNode.next;
			}
			postWriter.close();

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Data Saved");
			alert.setHeaderText(null);
			alert.setContentText("All data saved to desktop successfully!");
			alert.showAndWait();
		} catch (IOException ex) {
			ex.printStackTrace();
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Failed to Save Data");
			alert.setContentText("An error occurred while saving the data.");
			alert.showAndWait();
		}
	}

	// Alert method to show error or information alerts in methods
	private void showAlert(String title, String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}