import org.apache.commons.lang3.ObjectUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendAudio;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiValidationException;

import static java.util.Objects.isNull;

public class ReminderBot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botapi = new TelegramBotsApi();
        try{
            botapi.registerBot(new ReminderBot());
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return "ArticleReminderBot";
        //возвращаем юзера
    }

    @Override
    public String getBotToken() {
        return "554141774:AAHT89N-sepPsGZMIGRVdED9kv_0VDbXSF4";
        //Токен бота
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && !(update.getMessage().getChatId().equals(update.getMessage().getForwardFromChat().getId()))) {
            ForwardMessage message= new ForwardMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setFromChatId(update.getMessage().getChat().getId())
                    .setMessageId(update.getMessage().getMessageId());
            try {
                 forwardMessage(message);// Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage()){
            ReplyKeyboardMarkup replyKeyboardMarkup=new ReplyKeyboardMarkup();
            
            SendMessage message=new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("When should I remind you?")
                    .setReplyMarkup
        }
    }

    @SuppressWarnings("deprecation") // Означает то, что в новых версиях метод уберут или заменят
    private void sendMsg(Message msg, String text) {
        SendMessage s = new SendMessage();
        s.setChatId(msg.getChatId()); // Боту может писать не один человек, и поэтому чтобы отправить сообщение, грубо говоря нужно узнать куда его отправлять
        s.setText(text);
        try { //Чтобы не крашнулась программа при вылете Exception
            sendMessage(s);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
}
