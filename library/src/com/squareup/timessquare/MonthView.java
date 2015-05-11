// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MonthView extends LinearLayout {
  TextView title;
  CalendarGridView grid;
  private Listener listener;
  private List<CalendarCellDecorator> decorators;
  private boolean isRtl;

  public static MonthView create(ViewGroup parent, LayoutInflater inflater,
      DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
      int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
      int headerTextColor, Locale locale) {
    return create(parent, inflater, weekdayNameFormat, listener, today, dividerColor,
        dayBackgroundResId, dayTextColorResId, titleTextColor, displayHeader, headerTextColor, null,
        locale);
  }

  public static MonthView create(ViewGroup parent, LayoutInflater inflater,
      DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
      int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
      int headerTextColor, List<CalendarCellDecorator> decorators, Locale locale) {
    final MonthView view = (MonthView) inflater.inflate(R.layout.month, parent, false);
    view.setDividerColor(dividerColor);
    view.setDayTextColor(dayTextColorResId);
    view.setTitleTextColor(titleTextColor);
    view.setDisplayHeader(displayHeader);
    view.setHeaderTextColor(headerTextColor);

    if (dayBackgroundResId != 0) {
      view.setDayBackground(dayBackgroundResId);
    }

    final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);

    view.isRtl = isRtl(locale);
    int firstDayOfWeek = today.getFirstDayOfWeek();
    final CalendarRowView headerRow = (CalendarRowView) view.grid.getChildAt(0);
    for (int offset = 0; offset < 7; offset++) {
      today.set(Calendar.DAY_OF_WEEK, getDayOfWeek(firstDayOfWeek, offset, view.isRtl));
      final TextView textView = (TextView) headerRow.getChildAt(offset);
      textView.setText(weekdayNameFormat.format(today.getTime()));
    }
    today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);
    view.listener = listener;
    view.decorators = decorators;
    return view;
  }

  private static int getDayOfWeek(int firstDayOfWeek, int offset, boolean isRtl) {
    int dayOfWeek = firstDayOfWeek + offset;
    if (isRtl) {
      return 8 - dayOfWeek;
    }
    return dayOfWeek;
  }

  private static boolean isRtl(Locale locale) {
    // TODO convert the build to gradle and use getLayoutDirection instead of this (on 17+)?
    final int directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
    return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
        || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
  }

  public MonthView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDecorators(List<CalendarCellDecorator> decorators) {
    this.decorators = decorators;
  }

  public List<CalendarCellDecorator> getDecorators() {
    return decorators;
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    title = (TextView) findViewById(R.id.title);
    grid = (CalendarGridView) findViewById(R.id.calendar_grid);
  }

  public void init(MonthDescriptor month, List<List<MonthCellDescriptor>> cells,
      boolean displayOnly, Typeface titleTypeface, Typeface dateTypeface) {
    Logr.d("Initializing MonthView (%d) for %s", System.identityHashCode(this), month);
    long start = System.currentTimeMillis();
    title.setText(month.getLabel());

    final int numRows = cells.size();
    grid.setNumRows(numRows);
    for (int i = 0; i < 6; i++) {
      CalendarRowView weekRow = (CalendarRowView) grid.getChildAt(i + 1);
      weekRow.setListener(listener);
      if (i < numRows) {
        weekRow.setVisibility(VISIBLE);
        List<MonthCellDescriptor> week = cells.get(i);
        for (int c = 0; c < week.size(); c++) {
          MonthCellDescriptor cell = week.get(isRtl ? 6 - c : c);
          CalendarCellView cellView = (CalendarCellView) weekRow.getChildAt(c);

          String cellDate = Integer.toString(cell.getValue());
          if (!cellView.getText().equals(cellDate)) {
            cellView.setText(cellDate);
          }
          cellView.setEnabled(cell.isCurrentMonth());
          cellView.setClickable(!displayOnly);

          cellView.setSelectable(cell.isSelectable());
          cellView.setSelected(cell.isSelected());
          cellView.setCurrentMonth(cell.isCurrentMonth());
          cellView.setToday(cell.isToday());
          cellView.setRangeState(cell.getRangeState());
          cellView.setHighlighted(cell.isHighlighted());
          cellView.setTag(cell);

          if (null != decorators) {
            for (CalendarCellDecorator decorator : decorators) {
              decorator.decorate(cellView, cell.getDate());
            }
          }
        }
      } else {
        weekRow.setVisibility(GONE);
      }
    }

    if (titleTypeface != null) {
      title.setTypeface(titleTypeface);
    }
    if (dateTypeface != null) {
      grid.setTypeface(dateTypeface);
    }

    Logr.d("MonthView.init took %d ms", System.currentTimeMillis() - start);
  }
  public void init(MonthDescriptor month, List<List<MonthCellDescriptor>> cells,
                   boolean displayOnly, Typeface titleTypeface, Typeface dateTypeface,String strStart,String strEnd) {
    Logr.d("Initializing MonthView (%d) for %s", System.identityHashCode(this), month);
    long start = System.currentTimeMillis();
    title.setText(month.getLabel());

    final int numRows = cells.size();
    grid.setNumRows(numRows);
    for (int i = 0; i < 6; i++) {
      CalendarRowView weekRow = (CalendarRowView) grid.getChildAt(i + 1);
      weekRow.setListener(listener);
      if (i < numRows) {
        weekRow.setVisibility(VISIBLE);
        List<MonthCellDescriptor> week = cells.get(i);
        for (int c = 0; c < week.size(); c++) {
          MonthCellDescriptor cell = week.get(c);
          CalendarCellView cellView = (CalendarCellView) weekRow.getChildAt(c);
          cellView.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
          cellView.setText(String.format("%02d", cell.getValue()));
          cellView.setEnabled(cell.isCurrentMonth());
          cellView.setClickable(!displayOnly);

          cellView.setSelectable(cell.isSelectable());

          if(strStart.length()>0){


            String chkin = "\n"+strStart;
            String chkout = "\n"+strEnd;
            //備註cell字的長度
            int stringStart;
            int stringEnd;
            //備註cell字體大小
            int stringBig=getIntFromDimens(R.dimen.calendar_text_medium);
            int stringSmall=getIntFromDimens(R.dimen.calendar_text_small);
            String selectDate=Integer.toString(cell.getValue());
            if (cell.isSelected()) {
              cellView.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
              SpannableStringBuilder dateSelectState = new SpannableStringBuilder();
              dateSelectState.append(selectDate);
              stringStart=0;
              stringEnd=selectDate.length();
              dateSelectState.setSpan(new AbsoluteSizeSpan(stringBig), stringStart, stringEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

              dateSelectState.append(chkin);
              stringStart=stringEnd;
              stringEnd+=chkin.length();
              dateSelectState.setSpan(new AbsoluteSizeSpan(stringSmall), stringStart, stringEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

              cellView.setText(dateSelectState);
            }
            if (cell.getRangeState() == MonthCellDescriptor.RangeState.FIRST) {
              cellView.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
              SpannableStringBuilder dateSelectState = new SpannableStringBuilder();
              dateSelectState.append(selectDate);
              stringStart=0;
              stringEnd=selectDate.length();
              dateSelectState.setSpan(new AbsoluteSizeSpan(stringBig), stringStart, stringEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

              dateSelectState.append(chkin);
              stringStart=stringEnd;
              stringEnd+=chkin.length();
              dateSelectState.setSpan(new AbsoluteSizeSpan(stringSmall), stringStart, stringEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

              cellView.setText(dateSelectState);
            }
            if (cell.getRangeState() == MonthCellDescriptor.RangeState.LAST) {
              cellView.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
              SpannableStringBuilder dateSelectState = new SpannableStringBuilder();
              dateSelectState.append(selectDate);
              stringStart=0;
              stringEnd=selectDate.length();
              dateSelectState.setSpan(new AbsoluteSizeSpan(stringBig), stringStart, stringEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

              dateSelectState.append(chkout);
              stringStart=stringEnd;
              stringEnd+=chkout.length();
              dateSelectState.setSpan(new AbsoluteSizeSpan(stringSmall), stringStart, stringEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

              cellView.setText(dateSelectState);

            }
            if (cell.getRangeState() == MonthCellDescriptor.RangeState.MIDDLE) {
              cellView.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
              cellView.setText(Integer.toString(cell.getValue()));
            }

          }

          cellView.setSelected(cell.isSelected());
          cellView.setCurrentMonth(cell.isCurrentMonth());
          cellView.setToday(cell.isToday());
          cellView.setRangeState(cell.getRangeState());
          cellView.setHighlighted(cell.isHighlighted());
          cellView.setTag(cell);

          if (null != decorators) {
            for (CalendarCellDecorator decorator : decorators) {
              decorator.decorate(cellView, cell.getDate());
            }
          }
        }
      } else {
        weekRow.setVisibility(GONE);
      }
    }

    if (titleTypeface != null) {
      title.setTypeface(titleTypeface);
    }
    if (dateTypeface != null) {
      grid.setTypeface(dateTypeface);
    }

    Logr.d("MonthView.init took %d ms", System.currentTimeMillis() - start);
  }

  public void setDividerColor(int color) {
    grid.setDividerColor(color);
  }

  public void setDayBackground(int resId) {
    grid.setDayBackground(resId);
  }

  public void setDayTextColor(int resId) {
    grid.setDayTextColor(resId);
  }

  public void setTitleTextColor(int color) {
    title.setTextColor(color);
  }

  public void setDisplayHeader(boolean displayHeader) {
    grid.setDisplayHeader(displayHeader);
  }

  public void setHeaderTextColor(int color) {
    grid.setHeaderTextColor(color);
  }

  public interface Listener {
    void handleClick(MonthCellDescriptor cell);
  }
  private int getIntFromDimens(int index) {
    int result = this.getResources().getDimensionPixelSize(index);
    return result;
  }
}
