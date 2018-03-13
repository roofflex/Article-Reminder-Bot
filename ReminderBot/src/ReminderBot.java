import org.apache.commons.lang3.ObjectUtils;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendAudio;
import org.telegram.telegrambots.api.methods.send.SendMessage;
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

    @SuppressWarnings("deprecation")
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        InlineKeyboardMarkup questionmarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> questionkeyboard=new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow1=new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow2=new ArrayList<>();
        List<InlineKeyboardButton> buttonsrow3=new ArrayList<>();
        buttonsrow1.add(new InlineKeyboardButton().setText("Today").setCallbackData("Today"));
        buttonsrow1.add(new InlineKeyboardButton().setText("Tomorrow").setCallbackData("Tomorrow"));
        buttonsrow1.add(new InlineKeyboardButton().setText("Overmorrow").setCallbackData("Overmorrow"));
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
            ForwardMessage fwrd = new ForwardMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setFromChatId(update.getMessage().getChat().getId())
                    .setMessageId(update.getMessage().getMessageId());
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("When should I remind you?")
                    .setReplyMarkup(questionmarkup);
            Integer unixTime = update.getMessage().getDate();
            Date date = new Date(unixTime * 1000L);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("k:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
            String time = simpleDateFormat.format(date);
            Long chat_id=update.getMessage().getChatId();
            try {
                //forwardMessage(fwrd);// Call method to send the message
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            final ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
            final Runnable sender=new Runnable() {
                @Override
                public void run() {
                    try{
                        forwardMessage(fwrd);
                    } catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                };
            };
            scheduledExecutorService.schedule(sender,20 , TimeUnit.SECONDS);
        }
            if (update.hasCallbackQuery()) {
                ForwardMessage fwrd = new ForwardMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(update.getMessage().getChatId())
                        .setFromChatId(update.getMessage().getChat().getId())
                        .setMessageId(update.getMessage().getMessageId());
                int delay = 0;
                String string = "";
                String string2="";
                try {
                    Integer unixtime1=update.getCallbackQuery().getMessage().getDate();
                    LocalDate ld=LocalDate.now();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd k:mm");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));
                    Date date = sdf.parse(ld+" "+update.getCallbackQuery().getData());
                    Integer unixtime2=(int) (date.getTime()/1000);
                    //Long i=unixtime2-unixtime1;
                    string=unixtime1.toString(); //sdf.format(date);
                    string2=unixtime2.toString(); //sdf.format(new Date(update.getCallbackQuery().getMessage().getDate()*1000L));
                    delay=unixtime2-unixtime1;

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    SendMessage sendMessage = new SendMessage()
                            .setChatId(update.getCallbackQuery().getMessage().getChatId())
                            .setText(string);
                    sendMessage(sendMessage);
                    SendMessage sendMessage2 = new SendMessage()
                            .setChatId(update.getCallbackQuery().getMessage().getChatId())
                            .setText(string2);
                    sendMessage(sendMessage2);
                    final ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
                    final Runnable sender=new Runnable() {
                        @Override
                        public void run() {
                            try{
                                forwardMessage(fwrd);
                            } catch (TelegramApiException e){
                                e.printStackTrace();
                            }
                        };
                    };
                    scheduledExecutorService.schedule(sender,20 , TimeUnit.SECONDS);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
        else if (update.hasMessage()){
            ReplyKeyboardMarkup replyKeyboardMarkup=new ReplyKeyboardMarkup();

            SendMessage message=new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("When should I remind you?");
        }
    }


}
