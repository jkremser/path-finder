package cz.muni.fi.mobileIDS.src;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Jiri Kremser
 * 
 * Searcher
 */
public class Searcher implements Searchable {

    private int time;
    private int fromStationId;
    private int toStationId;
    private static final LineUtils LU = LineUtils.getInstance();
    private Vector linePool;
    private Hashtable stations;
    private Hashtable steps;
    private static int min = Integer.MAX_VALUE;


    /**
     * Constructor
     * 
     * @param time Time
     * @param day Day
     * @param fromStationId ID of start station
     * @param toStationId ID of end station
     */
    public Searcher(int time, String day, int fromStationId, int toStationId) {
        this.time = time;
        this.fromStationId = fromStationId;
        this.toStationId = toStationId;
        this.steps = new Hashtable(50);
        this.linePool = new Vector(50);
        this.stations = new Hashtable(50);
        LU.setDay(day);
    }

    /**
     * Search
     * 
     * @return instance of Step
     */
    public Step search() {
        Station start = StationUtils.getStationById((short) fromStationId);
        start.setLength((short) 0);
        stations.put(start, start);
        steps.put(start, new Step((short) fromStationId, (short) 1, (short) 0, (short) 0, null)); // null = dno
        int maxIter = 4000;
        boolean endStationFound = false;
        int endStationMin = Integer.MAX_VALUE;
        Hashtable minimums = null;
        boolean directionFlag = true;


        while (maxIter-- > 0 && !stations.isEmpty()) {
            //choose node with the shortest length
            Station curStation = getBestStation();
            //foreach neighbor of the best node do..
            short[] lineIds = curStation.getLines();
            int i = 0;
            while (i < lineIds.length) {

                //get line 
                Line line = new Line(lineIds[i], null, null);
                line.setDirection(directionFlag ? 'A' : 'B');
                if (!directionFlag) {
                    i++;
                }
                directionFlag = !directionFlag;


                boolean isInPool = linePool.contains(line);
                line = getLine(line, curStation, isInPool);
                if (line.getId() == - 1) { //zastavka neobsahuje zaznam pro tuto linku (spatny den)
                    //remove from pool
                    if (isInPool) {
                        linePool.removeElement(line);
                    }
                    line = null;
                    continue;
                }

                boolean owned = (curStation.getLineId() == 0 || (curStation.getLineId() == line.getId() && line.getStations()[line.getCurrentStationId() /*curStation.getDirection() == line.getDirection()*/] == curStation.getUid())); // curStation.id == 0 --> undef (start)
                if ((!owned && isInPool) || line.getCurrentStationId() + 1 >= line.getStations().length) {
                    if (line.getCurrentStationId() + 1 >= line.getStations().length) {
                        linePool.removeElement(line); //end station
                    }
                    line = null;
                    continue; //line is at another station
                }

                line.setJourneyTime((short) (line.getJourneyTime() + line.getDelays()[line.getCurrentStationId()]));
                line.setCurrentStationId((byte) (line.getCurrentStationId() + 1)); // move

                Station station = StationUtils.getStationByUid(line.getStations()[line.getCurrentStationId()]).clone();
                station.setLineId(line.getId());
                station.setLineDirection(line.getDirection());

                station.evalLength(curStation, line); // evaluate length from start node to this station
                    
                Step s1 = (Step) steps.get(curStation);
                int a1 = line.getNextDepartureInMin();
                int a2 = line.getJourneyTime();
                int a3 = line.getId();
                int a4 = station.getId();
                Step s = new Step((short) a4, (short) a3, (short) a2, (short) a1, s1); // PERFORMANCEE
                steps.put(station, s);


                if (station.getId() == toStationId) { //end station has been found
                    endStationFound = true;
                    endStationMin = station.getLength();
                    if (minimums == null) {
                        minimums = new Hashtable();
                    }
                    minimums.put(station, s);
                //end station has been found :]
                }
                s = null;


                line.setNextDepartureInMin((short) 0); //initial waiting
                if (!isInPool) {
                    linePool.addElement(line); //prestup / nastup
                }
                if (endStationFound) {
                    if (endStationMin <= min) {
                        int localMin = Integer.MAX_VALUE;
                        Enumeration e = minimums.keys();
                        while (e.hasMoreElements()) {
                            int current = ((Station) e.nextElement()).getLength();
                            if (current < localMin) {
                                localMin = current;
                            }
                        }
                        Step[] steps = new Step[10];
                        int j = 0;
                        int minSize = Integer.MAX_VALUE;
                        int minIndex = 0;
                        e = minimums.keys();
                        while (e.hasMoreElements()) {
                            Station st = (Station) e.nextElement();
                            int current = st.getLength();
                            if (current == localMin) {
                                Step step = (Step) minimums.get(st);
                                steps[j++] = step;
                                int size = 0;
                                while (step.getPrevious() != null) {
                                    size++;
                                    step = step.getPrevious();
                                }
                                if (minSize > size) {
                                    minSize = size;
                                    minIndex = j - 1;
                                }
                            }
                        }

                        return steps[minIndex];
                    }
                }
                stations.put(station, station);
            }
        }
        System.out.println("iter = " + maxIter);
        return null; //nothing has been found
    }

    private Station getBestStation() {
        Station currentStation;
        min = Integer.MAX_VALUE;
        Station minStation = null;


        Enumeration e = stations.keys();
        while (e.hasMoreElements()) {
            currentStation = (Station) e.nextElement();
            int length = currentStation.getLength();
            if (length < min) {
                min = currentStation.getLength();
                minStation = currentStation;
            }
        }

        stations.remove(minStation);
        return minStation;
    }

    private Line getLine(Line line, Station curStation, boolean isInPool) {
        Line l = null;
        if (isInPool) { //equals depends id and direction
            l = (Line) linePool.elementAt(linePool.indexOf(line)); //performance?
        } else {
            l = LU.getLine(line.getId(), curStation.getUid(), time + curStation.getLength(), line.getDirection());
        }

        return l;
    }
}
