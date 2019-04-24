package uk.gov.companieshouse.extensions.api.reasons;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReasonsService {

    public ExtensionReasonEntity insertExtensionsReason(ExtensionCreateReason extensionCreateReason) {
        ExtensionReasonEntityBuilder extensionReasonEntityBuilder = ExtensionReasonEntityBuilder.getInstance();

        String reason = extensionCreateReason.getReason();
        if (StringUtils.isNotBlank(reason)) {
            extensionReasonEntityBuilder.withReason(reason);
        }

        String additionalText = extensionCreateReason.getAdditionalText();
        if (StringUtils.isNotBlank(additionalText)) {
            extensionReasonEntityBuilder.withAdditionalText(additionalText);
        }

        LocalDate startOn = extensionCreateReason.getStartOn();
        if (startOn != null) {
            extensionReasonEntityBuilder.withStartOn(startOn);
        }

        LocalDate endOn = extensionCreateReason.getEndOn();
        if (endOn != null) {
            extensionReasonEntityBuilder.withEndOn(endOn);
        }

        //TODO insert into mongo
        return extensionReasonEntityBuilder.build();
    }
}
