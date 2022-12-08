package swidgets;

import io.vavr.control.Option;
import nz.sodium.Cell;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

public class SDateField extends JComponent {
    public SDateField() {
        this(new GregorianCalendar());
    }

    private static final String[] months = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public SDateField(Calendar cal) {
        Vector<Integer> years = new Vector<>();
        Calendar now = new GregorianCalendar();
        for (int y = now.get(Calendar.YEAR) - 10; y <= now.get(Calendar.YEAR) + 10; y++)
            years.add(y);
        SComboBox<Integer> year = new SComboBox<>(years);
        year.setSelectedItem(cal.get(Calendar.YEAR));
        SComboBox<String> month = new SComboBox<>(months);
        Vector<Integer> days = new Vector<>();
        for (int d = 1; d <= 31; d++)
            days.add(d);
        month.setSelectedItem(months[cal.get(Calendar.MONTH)]);
        SComboBox<Integer> day = new SComboBox<>(days);
        day.setSelectedItem(cal.get(Calendar.DAY_OF_MONTH));
        setLayout(new FlowLayout());
        add(year);
        add(month);
        add(day);
        Cell<Option<Integer>> monthIndex = month.selectedItem.map(
                ostr -> {
                    if (ostr.isDefined()) {
                        for (int i = 0; i < months.length; i++)
                            if (months[i].equals(ostr.get()))
                                return Option.some(i);
                    }
                    return Option.none();
                });
        date = year.selectedItem.lift(monthIndex, day.selectedItem,
                (oy, om, od) -> oy.isDefined() && om.isDefined() && od.isDefined()
                        ? new GregorianCalendar(oy.get(), om.get(), od.get())
                        : new GregorianCalendar());
    }

    public final Cell<Calendar> date;
}

