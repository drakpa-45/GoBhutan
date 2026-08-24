package com.goBhutan.adminPanel.notification.template;

import com.goBhutan.adminPanel.notification.exception.NotificationException;
import com.goBhutan.adminPanel.notification.model.NotificationTemplateRecord;
import com.goBhutan.adminPanel.notification.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class TemplateService {
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{([a-zA-Z0-9_.-]+)}}");
    private final TemplateRepository repository;

    public record Rendered(String title, String body) {}

    public Rendered render(String code, Map<String,String> variables) {
        NotificationTemplateRecord template = repository.findActive(code)
                .orElseThrow(() -> new NotificationException("Invalid or inactive template: " + code));
        Map<String,String> safe = variables == null ? Map.of() : variables;
        return new Rendered(
                replace(template.getTitle(), safe),
                replace(template.getBody(), safe));
    }

    private String replace(String input, Map<String,String> variables) {
        Matcher matcher = VARIABLE.matcher(input);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.get(key);
            if (value == null) throw new NotificationException("Missing template variable: " + key);
            matcher.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
