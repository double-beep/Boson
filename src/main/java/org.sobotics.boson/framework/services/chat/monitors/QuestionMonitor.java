package org.sobotics.boson.framework.services.chat.monitors;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.sobotics.boson.framework.exceptions.StackExchangeApiException;
import org.sobotics.boson.framework.model.chat.ChatRoom;
import org.sobotics.boson.framework.model.stackexchange.Question;
import org.sobotics.boson.framework.services.chat.filters.Filter;
import org.sobotics.boson.framework.services.chat.printers.PrinterService;
import org.sobotics.boson.framework.services.dashboard.DashboardService;
import org.sobotics.boson.framework.services.data.ApiService;

public class QuestionMonitor extends Monitor<Question, Question> {

    private Instant previousLastActivityDate;

    public QuestionMonitor(ChatRoom room, int frequency, String site, String apiKey, String apiToken,
                           Filter<Question>[] filters, PrinterService<Question> printer,
                           DashboardService dashboardService) {
        super(room, frequency, site, apiKey, filters, printer, apiToken, dashboardService);
        previousLastActivityDate = Instant.now().minusSeconds(60);
    }

    @Override
    protected void monitor(ChatRoom room, String site, Filter<Question>[] filters, PrinterService<Question> printer,
                           ApiService apiService, DashboardService dashboard) throws IOException {
        List<Question> questions;
        try {
            questions = apiService.getQuestions(site, 1, 100, null);
            questions = questions.stream().filter(question -> question.getLastActivityDate().isAfter(previousLastActivityDate)).collect(Collectors.toList());
            if (questions.size() > 0) {
                previousLastActivityDate = questions.get(0).getLastActivityDate();
            }

        } catch (StackExchangeApiException e) {
            room.getRoom().send("Bot not configured properly");
            e.printStackTrace();
            return;
        }
        for (Question question : questions) {
            for (Filter<Question> filter : filters) {
                if (filter.filter(question)) {
                    room.getRoom().send(getFinalPrintString(printer, dashboard, question));
                }
            }
        }
    }
}
