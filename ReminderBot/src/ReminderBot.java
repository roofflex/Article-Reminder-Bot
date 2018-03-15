import org.apache.commons.lang3.ObjectUtils;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendAudio;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiValidationException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Objects.isNull;

public class ReminderBot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botapi = new TelegramBotsApi();         //Api initialization
        try {
            botapi.registerBot(new ReminderBot());          //Bot registration
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "ArticleReminderBot";            //Bot's name
    }

    @Override
    public String getBotToken() {
        return "554141774:AAHT89N-sepPsGZMIGRVdED9kv_0VDbXSF4";         //Bot unique Token
    }

    protected ForwardMessage forwardArticle = null;               //Init of ForwardMessage here to pass it to thread
    protected InlineKeyboardMarkup newmarkup = new InlineKeyboardMarkup();
    LocalDate ld = LocalDate.now();
    int dateonce = 0;
    int timeonce = 0;
    int delay;
    ScheduledExecutorService scheduledExecutorService;
    Runnable sender;

    @SuppressWarnings("deprecation")
    @Override
    public void onUpdateReceived(Update update) {
        SendMessage answerMessage;
        InlineKeyboardMarkup questionmarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> questionkeyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow1 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow2 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow3 = new ArrayList<>();
        buttonsrow1.add(new InlineKeyboardButton().setText("Today").setCallbackData("Today"));
        buttonsrow1.add(new InlineKeyboardButton().setText("Tomorrow").setCallbackData("Tomorrow"));
        buttonsrow1.add(new InlineKeyboardButton().setText("Overmorrow").setCallbackData("Overmorrow"));        //Inline keyboard markup
        buttonsrow2.add(new InlineKeyboardButton().setText("8:00").setCallbackData("8:00"));
        buttonsrow2.add(new InlineKeyboardButton().setText("10:00").setCallbackData("10:00"));
        buttonsrow2.add(new InlineKeyboardButton().setText("12:00").setCallbackData("12:00"));
        buttonsrow3.add(new InlineKeyboardButton().setText("14:00").setCallbackData("14:00"));
        buttonsrow3.add(new InlineKeyboardButton().setText("17:00").setCallbackData("17:00"));
        buttonsrow3.add(new InlineKeyboardButton().setText("20:00").setCallbackData("20:00"));
        questionkeyboard.add(buttonsrow1);
        questionkeyboard.add(buttonsrow2);
        questionkeyboard.add(buttonsrow3);
        questionmarkup.setKeyboard(questionkeyboard);

        if (update.hasMessage() && !(update.getMessage().getChatId().equals(update.getMessage().getForwardFromChat().getId()))) {
            forwardArticle = new ForwardMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setFromChatId(update.getMessage().getChat().getId())       //Set info for ForwardMessage
                    .setMessageId(update.getMessage().getMessageId());
            answerMessage = new SendMessage()
                    .setChatId(update.getMessage().getChatId())             //Set info for answerMessage
                    .setText("When should I remind you?")
                    .setReplyMarkup(questionmarkup);
            newmarkup = questionmarkup;
            ld = LocalDate.now();
            dateonce = 0;
            timeonce = 0;
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            sender = new Runnable() {
                @Override
                public void run() {
                    try {
                        forwardMessage(forwardArticle);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                ;
            };
            try {
                sendMessage(answerMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (update.hasCallbackQuery() && forwardArticle != null) {
            EditMessageReplyMarkup replyMarkup;
            if (update.getCallbackQuery().getData().contains("o")) {             //Getting the info about the day to post, since all day options have "o"
                dateonce++;
                if (dateonce > 1) {
                    List<InlineKeyboardButton> list = newmarkup.getKeyboard().get(0);
                    for (InlineKeyboardButton button : list) {
                        if (button.getText().contains("\u2022")) {
                            button.setText(button.getText().replace("\u2022", ""));          // !!!!NEED TO TERMINATE RUNNING THREAD
                        }
                    }
                    scheduledExecutorService.shutdownNow();
                    dateonce--;
                    scheduledExecutorService = Executors.newScheduledThreadPool(1);
                }
                for (List<InlineKeyboardButton> list : newmarkup.getKeyboard()) {
                    for (InlineKeyboardButton button : list) {
                        if (button.getCallbackData().equals(update.getCallbackQuery().getData())) {
                            button.setText("\u2022" + button.getText() + "\u2022");
                        }
                    }
                }
                replyMarkup = new EditMessageReplyMarkup()
                        .setChatId(update.getCallbackQuery().getMessage().getChatId())
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setReplyMarkup(newmarkup);
                switch (update.getCallbackQuery().getData()) {
                    case "Tomorrow":
                        ld.plusDays(1);
                        break;
                    case "Overmorrow":
                        ld.plusDays(2);
                        break;
                    default:
                        break;
                }
            } else {
                timeonce++;
                if (timeonce > 1) {
                    for (int i = 1; i < 3; i++) {
                        List<InlineKeyboardButton> list = newmarkup.getKeyboard().get(i);
                        for (InlineKeyboardButton button : list) {
                            if (button.getText().contains("\u2022")) {
                                button.setText(button.getText().replace("\u2022", ""));          // !!!!NEED TO TERMINATE RUNNING THREAD
                            }
                        }
                    }
                    scheduledExecutorService.shutdownNow();
                    timeonce--;
                    scheduledExecutorService = Executors.newScheduledThreadPool(1);
                }
                delay = 0;
                try {
                    Integer unixtimenow = update.getCallbackQuery().getMessage().getDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(unixtimenow * 1000L));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd k:mm");
                    sdf.setTimeZone(calendar.getTimeZone());
                    Date date = sdf.parse(ld + " " + update.getCallbackQuery().getData());
                    Integer unixtimesend = (int) (date.getTime() / 1000);
                    delay = unixtimesend - unixtimenow;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                for (List<InlineKeyboardButton> list : newmarkup.getKeyboard()) {
                    for (InlineKeyboardButton button : list) {
                        if (button.getCallbackData().equals(update.getCallbackQuery().getData())) {
                            button.setText("\u2022" + button.getText() + "\u2022");
                        }
                    }
                }
                replyMarkup = new EditMessageReplyMarkup()
                        .setChatId(update.getCallbackQuery().getMessage().getChatId())
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setReplyMarkup(newmarkup);
                if (delay < 0) {
                    SendMessage wrongtime = new SendMessage()
                            .setChatId(update.getCallbackQuery().getMessage().getChatId())
                            .setText("Wrong time. Please, select a valid time");
                    for (List<InlineKeyboardButton> list : newmarkup.getKeyboard()) {
                        for (InlineKeyboardButton button : list) {
                            if (button.getText().contains("\u2022")) {
                                button.setText(button.getText().replace("\u2022", ""));          // !!!!NEED TO TERMINATE RUNNING THREAD
                            }
                        }
                    }
                    timeonce = 0;
                    dateonce = 0;
                }
                try {
                    editMessageReplyMarkup(replyMarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                if (dateonce == 1 && timeonce == 1) {
                    scheduledExecutorService.schedule(sender, delay, TimeUnit.SECONDS);
                    SendMessage messageisset = new SendMessage()
                            .setChatId(update.getCallbackQuery().getMessage().getChatId())
                            .setText("The message has been queued");
                    try {
                        sendMessage(messageisset);
                        System.out.println(delay);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

