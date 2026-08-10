package git.xlamid.eventmanagerservice.event.util;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class EventSorter {

    public Sort getSorts() {
        return addSort("date", false)
                .and(addSort("name", true));
    }

    private Sort addSort(String property, boolean asc) {
        return asc ?
                Sort.by(Sort.Order.asc(property).nullsLast()) :
                Sort.by(Sort.Order.desc(property).nullsLast());
    }
}