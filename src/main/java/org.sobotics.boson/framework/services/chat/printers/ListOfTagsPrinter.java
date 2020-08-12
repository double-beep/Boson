package org.sobotics.boson.framework.services.chat.printers;

import java.util.List;

import org.sobotics.boson.framework.model.stackexchange.Tag;

public class ListOfTagsPrinter extends PrinterService<List<Tag>> {
    private String sitename;

    public ListOfTagsPrinter(String sitename) {
        this.sitename = sitename;
    }

    public String print(List<Tag> tags, String dashboard, String intro) {

        StringBuilder message = new StringBuilder("New tags created: ");
        for (Tag tag : tags) {
            message.append("[tag:").append(tag.getName()).append("] ");
        }

        return getFinalPrintString(dashboard, intro, message.toString());

    }
}
