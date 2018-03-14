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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

public class ReminderBot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botapi = new TelegramBotsApi();         //Api initialization
        try{
            botapi.registerBot(new ReminderBot());          //Bot registration
        } catch (TelegramApiException e){
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

    protected ForwardMessage forwardArticle=null;

    @SuppressWarnings("deprecation")
    @Override
    public void onUpdateReceived(Update update) {
        SendMessage answerMessage;

        InlineKeyboardMarkup questionmarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> questionkeyboard=new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow1=new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow2=new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow3=new ArrayList<>();
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
            forwardArticle=new ForwardMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setFromChatId(update.getMessage().getChat().getId())
                    .setMessageId(update.getMessage().getMessageId());
            answerMessage=new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("When should I remind you?")
                    .setReplyMarkup(questionmarkup);
            try {
                sendMessage(answerMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (update.hasCallbackQuery() && forwardArticle!=null) {
            LocalDate ld=LocalDate.now();
            if (update.getCallbackQuery().getData().contains("o")){         //Getting the info about the day to post, since all day options have "o"
                switch (update.getCallbackQuery().getData()){
                    case "Tomorrow": ld.plusDays(1); break;
                    case "Overmorrow": ld.plusDays(2); break;
                    default: break;
                }
            } else {
//            InlineKeyboardMarkup newmarkup=questionmarkup;
//            for(List<InlineKeyboardButton> list: newmarkup.getKeyboard()){
//                for(InlineKeyboardButton button:list){
//                    if (button.getCallbackData()==update.getCallbackQuery().getData()){
//                        button.setText("\u2022"+button.getText());
//                    }
//                }
//            }
//            EditMessageReplyMarkup replyMarkup=new EditMessageReplyMarkup()
//                    .setChatId(update.getCallbackQuery().getMessage().getChatId())
//                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
//                    .setReplyMarkup(newmarkup);
                int delay = 0;
                try {
                    Integer unixtimenow = update.getCallbackQuery().getMessage().getDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd k:mm");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));
                    Date date = sdf.parse(ld + " " + update.getCallbackQuery().getData());
                    Integer unixtimesend = (int) (date.getTime() / 1000);
                    delay = unixtimesend - unixtimenow;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//                ForwardMessage fwrdMessage=forwardArticle;
//                    final ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
//                    final Runnable sender=new Runnable() {
//                        @Override
//                        public void run() {
//                            try{
//                                forwardMessage(fwrdMessage);
//                            } catch (TelegramApiException e){
//                                e.printStackTrace();
//                            }
//                        };
//                    };
//                    scheduledExecutorService.schedule(sender,30, TimeUnit.SECONDS);

            }
            }
    }
}
