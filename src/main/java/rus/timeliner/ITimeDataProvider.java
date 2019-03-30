/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

public interface ITimeDataProvider {
    double getSelectedTime();
    double getBeginTime();
    double getEndTime();
    double getMinTime();
    double getMaxTime();
    double getTime0();
    double getTime1();
    double getMinTimeInterval();
    void setStartFinishTime(double time0, double time1);
    void setSelectedTime(double time, boolean ensureVisible);
    void resetStartFinishTime();
    int getNameWidth();
    void setNameWidth(int width);
    int getTimeSpace();
}
