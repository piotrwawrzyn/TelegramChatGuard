import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

	private final String botToken = "660439874:AAGrTMV1nvC5xdFYcrKwW2cLkUf3LBGx610";
	private final String botUsername = "HubiiChatBot";


	// Username tolerance number in characters. If name + lastName >
	private final int maxUsernameCharacters = 40;

	private int messageID;

	// i) Chat ID. Hardcode to keep the bot usable just in one specific group.
	private String chatID;

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public String getBotUsername() {
		return botUsername;
	}

	@Override
	public void onUpdateReceived(Update update) {
		// ii) Dynamic Chat ID. Delete if hardcoded.
		chatID = update.getMessage().getChat().getId().toString();
		messageID = update.getMessage().getMessageId();

		List<User> newUsers = new ArrayList<User>();
		User leavingUser = null;
		
		if (update.hasMessage()) {
			if (update.getMessage().getNewChatMembers() != null || update.getMessage().getLeftChatMember() != null) {

				// At least one user joined the chat.
				newUsers = update.getMessage().getNewChatMembers();
				leavingUser = update.getMessage().getLeftChatMember();
				
				List<User> suspectedUsers = new ArrayList<User>();
				
				if(newUsers != null)
					suspectedUsers.addAll(newUsers);
				
				if(leavingUser != null)
					suspectedUsers.add(leavingUser);
				
				checkUsers(suspectedUsers);

			}
		}

	}

	private void checkUsers(List<User> newUsers) {

		// We ban every user that has abusive nickname. If any user in the list
		// has abusive name we delete the whole message.

		User currUser = null;
		String firstName = null;
		String lastName = null;

		boolean isSomeoneAbusing = false;

		for (int i = 0; i < newUsers.size(); i++) {
			currUser = newUsers.get(i);
			firstName = StringUtils.defaultString(currUser.getFirstName(), "");
			lastName = StringUtils.defaultString(currUser.getLastName(), "");

			if (firstName.length() + lastName.length() > maxUsernameCharacters) {
				isSomeoneAbusing = true;
				
				// Restrict the abuser but not kick him to not generate another bad message.
				restrictAbuser(currUser);
			}

		}

		if (isSomeoneAbusing) {
			deleteAbusiveMessage();
		}

	}

	private void restrictAbuser(User currUser) {
		RestrictChatMember badGuy = new RestrictChatMember();
		badGuy.setUserId(currUser.getId());
		badGuy.setChatId(chatID);
		badGuy.setCanAddWebPagePreviews(false);
		badGuy.setCanSendMediaMessages(false);
		badGuy.setCanSendMessages(false);
		badGuy.setCanSendOtherMessages(false);


		try {
			restrictChatMember(badGuy);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		

	}

	private void deleteAbusiveMessage() {

		DeleteMessage abusiveMessage = new DeleteMessage();
		abusiveMessage.setChatId(chatID);
		abusiveMessage.setMessageId(messageID);

		try {
			deleteMessage(abusiveMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
