package Models;

import java.util.List;

public class WeeklyShiftGroup {
    private String label;
    private int weekNumber;
    private List<StaffShiftViewModel> shifts;
    private int shiftCount;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public List<StaffShiftViewModel> getShifts() { return shifts; }
    public void setShifts(List<StaffShiftViewModel> shifts) { this.shifts = shifts; }

    public int getShiftCount() { return shiftCount; }
    public void setShiftCount(int shiftCount) { this.shiftCount = shiftCount; }
}
