package org.hazelcast.jet.demo;

import com.hazelcast.internal.json.JsonObject;
import org.hazelcast.jet.demo.types.WakeTurbulanceCategory;
import com.hazelcast.jet.impl.util.Util;
import com.hazelcast.json.internal.JsonSerializable;

import java.io.Serializable;
import java.util.List;

import static org.hazelcast.jet.demo.util.Util.asDouble;
import static org.hazelcast.jet.demo.util.Util.asInt;
import static org.hazelcast.jet.demo.util.Util.asLong;
import static org.hazelcast.jet.demo.util.Util.asString;
import static org.hazelcast.jet.demo.util.Util.asStringArray;

/**
 * DTO represents an aircraft.
 */
public class Aircraft implements JsonSerializable, Serializable {

    enum VerticalDirection {
        UNKNOWN,
        CRUISE,
        ASCENDING,
        DESCENDING
    }

    /*
     * NOTE The attribute comments may refer to something called DO-260B without,
     * explicitly mentioning it. The clue is if the comment has a series of numbers
     * in brackets e.g. (2.2.xyz) this refers to a section in DO-260B, for further
     * information please refer to :
     * 
     * https://en.wikipedia.org/wiki/Automatic_Dependent_Surveillance
     *
     * (the full standards document itself is only commercially available)
     */



    /**
     * The time the file containing this aircraft data was generated, in seconds
     * since Jan 1 1970 00:00:00 GMT (the Unix epoch)
     */
    private Long now;

    /**
     * The 24-bit ICAO identifier of the aircraft, as 6 hex digits.
     * The identifier may start with ‘~’, this means that the address is a non-ICAO
     * address (e.g. from TIS-B).
     */
    private String hex;

    /**
     * The type of underlying messages / best source of current data for this
     * position / aircraft: (the following list is in order of which data is
     * preferentially used)
     * adsb_icao: messages from a Mode S or ADS-B transponder, using a 24-bit ICAO
     * address
     * adsb_icao_nt: messages from an ADS-B equipped “non-transponder” emitter e.g.
     * a ground vehicle, using a 24-bit ICAO address
     * adsr_icao: rebroadcast of ADS-B messages originally sent via another data
     * link e.g. UAT, using a 24-bit ICAO address
     * tisb_icao: traffic information about a non-ADS-B target identified by a
     * 24-bit ICAO address, e.g. a Mode S target tracked by secondary radar
     * adsc: ADS-C (received by monitoring satellite downlinks)
     * mlat: MLAT, position calculated arrival time differences using multiple
     * receivers, outliers and varying accuracy is expected.
     * other: miscellaneous data received via Basestation / SBS format, quality /
     * source is unknown.
     * mode_s: ModeS data from the planes transponder (no position transmitted)
     * adsb_other: messages from an ADS-B transponder using a non-ICAO address, e.g.
     * anonymized address
     * adsr_other: rebroadcast of ADS-B messages originally sent via another data
     * link e.g. UAT, using a non-ICAO address
     * tisb_other: traffic information about a non-ADS-B target using a non-ICAO
     * address
     * tisb_trackfile: traffic information about a non-ADS-B target using a
     * track/file identifier, typically from primary or Mode A/C radar
     */
    private String type;

    /**
     * The call sign, the flight name or aircraft registration as 8 chars (2.2.8.2.6)
     */
    private String flight;

    /**
     * The aircraft registration
     */
    private String r;

    /**
     * The aircraft type
     */
    private String t;

    /**
     * The aircraft barometric altitude in feet
     */
    private Integer alt_baro;

    /**
     * Geometric (GNSS / INS) altitude in feet referenced to the WGS84 ellipsoid
     */
    private Integer alt_geom;

    /**
     * Contains the altitude calculated from barometric and geometric altitudes
     */
    private long alt;

    /**
     * The ground speed in knots
     */
    private Double gs;

    /**
     * The indicated air speed in knots
     */
    private Integer ias;

    /**
     * The true air speed in knots
     */
    private Integer tas;

    /**
     * Mach number (airspeed)
     */
    private Double mach;

    /**
     * The wind direction, calculated from ground track, true heading
     */
    private Integer wd;

    /**
     * The wind speed, calculated from true airspeed and ground speed
     */
    private Integer ws;

    /**
     * Outer air temperature, calculated from mach number and true airspeed
     * (typically somewhat inaccurate at lower altitudes / mach numbers below 0.5)
     */
    private Integer oat;

    /**
     * Total air temperature, calculated from mach number and true airspeed
     * (typically somewhat inaccurate at lower altitudes / mach numbers below 0.5)
     */
    private Integer tat;

    /**
     * Track; true track over ground in degrees (0-359)
     */
    private Double track;

    /**
     * Rate of change of track, degrees/second
     */
    private Double track_rate;

    /**
     * Roll, degrees, negative is left roll
     */
    private Double roll;

    /**
     * Heading, degrees clockwise from magnetic north
     */
    private Double mag_heading;

    /**
     * Heading, degrees clockwise from true north
     * (usually only transmitted on ground, in the air usually derived from the
     * magnetic heading using magnetic model WMM2020)
     */
    private Double true_heading;

    /**
     * Rate of change of barometric altitude, feet/minute
     */
    private Integer baro_rate;

    /**
     * Rate of change of geometric (GNSS / INS) altitude, feet/minute
     */
    private Integer geom_rate;

    /**
     * Mode A code (Squawk), encoded as 4 octal digits
     */
    private String squawk;

    /**
     * ADS-B emergency/priority status, a superset of the 7×00 squawks
     * (2.2.3.2.7.8.1.1)
     */
    private String emergency;

    /**
     * Emitter category to identify particular aircraft or vehicle classes (values
     * A0 – D7) (2.2.3.2.5.2)
     * A- = Unspecified powered aircraft
     * A1 = Light (< 15 500 lbs.)
     * A2 = Small (15 500 to 75 000 lbs.)
     * A3 = Large (75 000 to 300 000 lbs.)
     * A4 = High Vortex Large(aircraft such as B-757)
     * A5 = Heavy (> 300 000 lbs.)
     * A6 = High Performance ( > 5 g acceleration and > 400kts)
     * A7 = Rotorcraft
     * B- = Unspecified unpowered aircraft or UAV or spacecraft
     * B1 = Glider/sailplane
     * B2 = Lighter-than-Air
     * B3 = Parachutist/Skydiver
     * B4 = Ultralight/hang-glider/paraglider
     * B5 = Reserved
     * B6 = Unmanned Aerial Vehicle
     * B7 = Space/Trans-atmospheric vehicle
     * C- = Unspecified ground installation or vehicle
     * C1 = Surface Vehicle – Emergency Vehicle
     * C2 = Surface Vehicle – Service Vehicle
     * C3 = Fixed Ground or Tethered Obstruction
     * C4 = Cluster Obstacle
     * C5 = Line Obstacle
     * C6-C7 = Reserved
     * D0 = No ADS-B Emitter Category Information
     * D1-D7 = Reserved
     */
    private String category;

    /**
     * The altimeter setting (QFE or QNH/QNE), hPa
     */
    private Double nav_qnh;

    /**
     * The selected altitude from the Mode Control Panel / Flight Control Unit
     * (MCP/FCU) or equivalent equipment
     */
    private Integer nav_altitude_mcp;

    /**
     * Selected altitude from the Flight Management System (FMS) (2.2.3.2.7.1.3.3)
     */
    private Integer nav_altitude_fms;

    /**
     * Selected heading (True or Magnetic is not defined in DO-260B, mostly Magnetic
     * as that is the de facto standard) (2.2.3.2.7.1.3.7)
     */
    private Double nav_heading;

    /**
     * : set of engaged automation modes: ‘autopilot’, ‘vnav’, ‘althold’,
     * ‘approach’, ‘lnav’, ‘tcas’
     */
    private String[] nav_modes;

    /**
     * The aircraft's position in decimal degrees
     */
    private double lat;

    /**
     * The aircraft's position in decimal degrees
     */
    private double lon;

    /**
     * If no ADS-B or MLAT position available, a rough estimated position for the
     * aircraft based on the receiver’s coordinates.
     */
    private double rr_lat;

    /**
     * If no ADS-B or MLON position available, a rough estimated position for the
     * aircraft based on the receiver’s coordinates.
     */
    private double rr_lon;

    /**
     * Navigation Integrity Category (2.2.3.2.7.2.6)
     */
    private Integer nic;

    /**
     * Radius of Containment, meters; a measure of position integrity derived from
     * NIC & supplementary bits. (2.2.3.2.7.2.6, Table 2-69)
     */
    private Integer rc;

    /**
     * How long ago (in seconds before “now”) the position was last updated
     */
    private Double seen_pos;

    /**
     * The ADS-B Version Number 0, 1, 2 (3-7 are reserved) (2.2.3.2.7.5)
     */
    private Integer version;

    /**
     * Navigation Integrity Category for Barometric Altitude (2.2.5.1.35)
     */
    private Integer nic_baro;

    /**
     * Navigation Accuracy for Position (2.2.5.1.35)
     */
    private Integer nac_p;

    /**
     * Navigation Accuracy for Velocity (2.2.5.1.19)
     */
    private Integer nac_v;

    /**
     * Source Integity Level (2.2.5.1.40)
     */
    private Integer sil;

    /**
     * Interpretation of SIL: unknown, perhour, persample
     */
    private String sil_type;

    /**
     * Geometric Vertical Accuracy (2.2.3.2.7.2.8)
     */
    private Integer gva;

    /**
     * System Design Assurance (2.2.3.2.7.2.4.6)
     */
    private Integer sda;

    /**
     * Flight status alert bit (2.2.3.2.3.2)
     */
    private Integer alert;

    /**
     * Flight status special position identification bit (2.2.3.2.3.2)
     */
    private Integer spi;

    /**
     * List of fields derived from MLAT data
     */
    private List<Object> mlat = null;

    /**
     * List of fields derived from TIS-B data
     */
    private List<Object> tisb = null;

    /**
     * The total number of Mode S messages received from this aircraft
     */
    private Integer messages;

    /**
     * How long ago (in seconds before “now”) a message was last received from this
     * aircraft
     */
    private Long seen;

    /**
     * The timestamp for this position record, initially calculated as the time the record was returned - seen
     * where seen is how long before "now" that the last update was received
     */
    private Long pos_time;

    /**
     * Recent average RSSI (signal power), in dbFS; this will always be negative.
     */
    private Double rssi;

    /**
     * Undocumented
     */
    private Double dst;

    /**
     * Undocumented
     */
    private Double dir;

    /**
     * Nearest airport to the aircraft at current location
     */
    private String airport;
    /**
     * Vertical direction of the aircraft.
     */
    private VerticalDirection verticalDirection = VerticalDirection.UNKNOWN;

    public Long getnow() {
        return now;
    }

    public void setnow(Long now) {
        this.now = now;
    }

    public String getid() {
        return this.gethex().replace("~", "");
    }

    public Boolean isgnd() {
        // If altGeom is 0 or if its not set and altBaro is 0 then assume the aircraft is grounded
        // if both are null assume the aircraft is grounded

        return this.alt_geom != null ? this.alt_geom == 0 :
                this.alt_baro == null || this.alt_baro == 0;
    }

    public Long getpos_time(long currentEpoch) {

        //Calculate posTime using the passed epoch
        long lastSeen = seen_pos != null && seen_pos > 0 ?
                            seen_pos.longValue() :
                                seen != null && seen > 0 ?
                                        seen :
                                            0L;
        // Returns the time the aircraft position last changed which is now - seenPos (or we use seen is that's not available)
        return currentEpoch - lastSeen;
    }

    public Long getpos_time() {

        long lastSeen = seen_pos != null && seen_pos > 0 ?
                seen_pos.longValue() :
                seen != null && seen > 0 ?
                        seen :
                        0L;

        // Returns the time the aircraft position last changed which is now - seenPos (or we use seen is that's not available)
        return now - lastSeen;
    }

    public void setpos_time(long pos_time) {
        this.pos_time = pos_time;
    }

    public WakeTurbulanceCategory getwtc() {

        if (category.equalsIgnoreCase("A1")) {
            return WakeTurbulanceCategory.LIGHT;
        } else if (category.equalsIgnoreCase("A2")) {
            return WakeTurbulanceCategory.SMALL;
        } else if (category.equalsIgnoreCase("A3")) {
            return WakeTurbulanceCategory.LARGE;
        } else if (category.equalsIgnoreCase("A4")) {
            return WakeTurbulanceCategory.HIGHVORTEXLARGE;
        } else if (category.equalsIgnoreCase("A5")) {
            return WakeTurbulanceCategory.HEAVY;
        } else if (category.equalsIgnoreCase("A6")) {
            return WakeTurbulanceCategory.HIGHPERFORMANCE;
        } else if (category.equalsIgnoreCase("A7")) {
            return WakeTurbulanceCategory.ROTORCRAFT;
        } else {
            return WakeTurbulanceCategory.NONE;
        }
    }

    /**
     * 
     * Getter and setters
     */
    public String gethex() {
        return hex;
    }

    public void sethex(String hex) {
        this.hex = hex;
    }

    public Aircraft withhex(String hex) {
        this.hex = hex;
        return this;
    }

    public String gettype() {
        return type;
    }

    public void settype(String type) {
        this.type = type;
    }

    public Aircraft withtype(String type) {
        this.type = type;
        return this;
    }

    public String getflight() {
        return flight;
    }

    public void setflight(String flight) {
        this.flight = flight;
    }

    public Aircraft withflight(String flight) {
        this.flight = flight;
        return this;
    }

    public String getr() {
        return r;
    }

    public void setr(String r) {
        this.r = r;
    }

    public Aircraft withr(String r) {
        this.r = r;
        return this;
    }

    public String gett() {
        return t;
    }

    public void sett(String t) {
        this.t = t;
    }

    public Aircraft witht(String t) {
        this.t = t;
        return this;
    }

    public Integer getalt_baro() {
        return alt_baro;
    }

    public void setalt_baro(Integer alt_baro) {
        this.alt_baro = alt_baro;
    }

    public Aircraft withaltbaro(Integer altBaro) {
        this.alt_baro = altBaro;
        return this;
    }

    public Integer getaltgeom() {
        return alt_geom;
    }

    public void setaltgeom(Integer alt_geom) {
        this.alt_geom = alt_geom;
    }

    public Aircraft withaltgeom(Integer altGeom) {
        this.alt_geom = altGeom;
        return this;
    }

    public Long getalt() {
        // Return precalculated altitude or calculate favouring the geometric altitude if asked for an altitude
        // otherwise return the barometric altitude

        long calculatedAltitude = alt > 0 ? alt : this.getaltgeom() != null && this.getaltgeom().longValue() > 0 ? this.getaltgeom().longValue() :
                this.getalt_baro() != null && this.getalt_baro().longValue() > 0 ? this.getalt_baro() : 0L;
        return calculatedAltitude;
    }

    public void setalt(Long alt) {
        this.alt = alt;
    }
    public Double getgs() {
        return gs;
    }

    public void setgs(Double gs) {
        this.gs = gs;
    }

    public Aircraft withgs(Double gs) {
        this.gs = gs;
        return this;
    }

    public Integer getias() {
        return ias;
    }

    public void setias(Integer ias) {
        this.ias = ias;
    }

    public Aircraft withias(Integer ias) {
        this.ias = ias;
        return this;
    }

    public Integer gettas() {
        return tas;
    }

    public void settas(Integer tas) {
        this.tas = tas;
    }

    public Aircraft withtas(Integer tas) {
        this.tas = tas;
        return this;
    }

    public Double getmach() {
        return mach;
    }

    public void setmach(Double mach) {
        this.mach = mach;
    }

    public Aircraft withmach(Double mach) {
        this.mach = mach;
        return this;
    }

    public Integer getwd() {
        return wd;
    }

    public void setwd(Integer wd) {
        this.wd = wd;
    }

    public Aircraft withwd(Integer wd) {
        this.wd = wd;
        return this;
    }

    public Integer getws() {
        return ws;
    }

    public void setws(Integer ws) {
        this.ws = ws;
    }

    public Aircraft withws(Integer ws) {
        this.ws = ws;
        return this;
    }

    public Integer getoat() {
        return oat;
    }

    public void setoat(Integer oat) {
        this.oat = oat;
    }

    public Aircraft withoat(Integer oat) {
        this.oat = oat;
        return this;
    }

    public Integer gettat() {
        return tat;
    }

    public void settat(Integer tat) {
        this.tat = tat;
    }

    public Aircraft withtat(Integer tat) {
        this.tat = tat;
        return this;
    }

    public Double gettrack() {
        return track;
    }

    public void settrack(Double track) {
        this.track = track;
    }

    public Aircraft withtrack(Double track) {
        this.track = track;
        return this;
    }

    public Double gettrack_rate() {
        return track_rate;
    }

    public void settrack_rate(Double track_rate) {
        this.track_rate = track_rate;
    }

    public Aircraft withtrack_rate(Double trackRate) {
        this.track_rate = trackRate;
        return this;
    }

    public Double getroll() {
        return roll;
    }

    public void setroll(Double roll) {
        this.roll = roll;
    }

    public Aircraft withRoll(Double roll) {
        this.roll = roll;
        return this;
    }

    public Double getmag_heading() {
        return mag_heading;
    }

    public void setmag_heading(Double mag_heading) {
        this.mag_heading = mag_heading;
    }

    public Aircraft withmag_heading(Double magHeading) {
        this.mag_heading = magHeading;
        return this;
    }

    public Double gettrue_heading() {
        return true_heading;
    }

    public void settrue_heading(Double true_heading) {
        this.true_heading = true_heading;
    }

    public Aircraft withtrue_heading(Double trueHeading) {
        this.true_heading = trueHeading;
        return this;
    }

    public Integer getbaro_rate() {
        return baro_rate;
    }

    public void setbaro_rate(Integer baro_rate) {
        this.baro_rate = baro_rate;
    }

    public Aircraft withbaro_rate(Integer baroRate) {
        this.baro_rate = baroRate;
        return this;
    }

    public Integer getgeom_rate() {
        return geom_rate;
    }

    public void setgeom_rate(Integer geom_rate) {
        this.geom_rate = geom_rate;
    }

    public Aircraft withgeom_rate(Integer geomRate) {
        this.geom_rate = geomRate;
        return this;
    }

    public String getsquawk() {
        return squawk;
    }

    public void setsquawk(String squawk) {
        this.squawk = squawk;
    }

    public Aircraft withsquawk(String squawk) {
        this.squawk = squawk;
        return this;
    }

    public String getemergency() {
        return emergency;
    }

    public void setemergency(String emergency) {
        this.emergency = emergency;
    }

    public Aircraft withemergency(String emergency) {
        this.emergency = emergency;
        return this;
    }

    public String getcategory() {
        return category;
    }

    public void setcategory(String category) {
        this.category = category;
    }

    public Aircraft withcategory(String category) {
        this.category = category;
        return this;
    }

    public Double getnav_qnh() {
        return nav_qnh;
    }

    public void setnav_qnh(Double nav_qnh) {
        this.nav_qnh = nav_qnh;
    }

    public Aircraft withnav_qnh(Double navQnh) {
        this.nav_qnh = navQnh;
        return this;
    }

    public Integer getnav_altitude_mcp() {
        return nav_altitude_mcp;
    }

    public void setnav_altitude_mcp(Integer navAltitudeMcp) {
        this.nav_altitude_mcp = navAltitudeMcp;
    }

    public Aircraft withnav_altitude_mcp(Integer navAltitudeMcp) {
        this.nav_altitude_mcp = navAltitudeMcp;
        return this;
    }

    public Double getlat() {
        return lat;
    }

    public void setlat(Double lat) {
        this.lat = lat;
    }

    public Aircraft withlat(Double lat) {
        this.lat = lat;
        return this;
    }

    public Double getlon() {
        return lon;
    }

    public void setlon(Double lon) {
        this.lon = lon;
    }

    public Aircraft withlon(Double lon) {
        this.lon = lon;
        return this;
    }

    public Double getrr_lat() {
        return rr_lat;
    }

    public void setrr_lat(Double rr_lat) {
        this.rr_lat = rr_lat;
    }

    public Aircraft witrrh_lat(Double rrLat) {
        this.rr_lat = rrLat;
        return this;
    }

    public Double getrr_lon() {
        return rr_lon;
    }

    public void setrr_lon(Double rr_lon) {
        this.rr_lon = rr_lon;
    }

    public Aircraft withrr_lon(Double rrLon) {
        this.rr_lon = rrLon;
        return this;
    }

    public Integer getnic() {
        return nic;
    }

    public void setnic(Integer nic) {
        this.nic = nic;
    }

    public Aircraft withnic(Integer nic) {
        this.nic = nic;
        return this;
    }

    public Integer getrc() {
        return rc;
    }

    public void setrc(Integer rc) {
        this.rc = rc;
    }

    public Aircraft withrc(Integer rc) {
        this.rc = rc;
        return this;
    }

    public Double getseen_pos() {
        return seen_pos;
    }

    public void setseen_pos(Double seen_pos) {
        this.seen_pos = seen_pos;
    }

    public Aircraft withseen_pos(Double seenPos) {
        this.seen_pos = seenPos;
        return this;
    }

    public Integer getversion() {
        return version;
    }

    public void setversion(Integer version) {
        this.version = version;
    }

    public Aircraft withversion(Integer version) {
        this.version = version;
        return this;
    }

    public Integer getnic_baro() {
        return nic_baro;
    }

    public void setnic_baro(Integer nic_baro) {
        this.nic_baro = nic_baro;
    }

    public Aircraft withnic_baro(Integer nicBaro) {
        this.nic_baro = nicBaro;
        return this;
    }

    public Integer getnac_p() {
        return nac_p;
    }

    public void setnac_p(Integer nac_p) {
        this.nac_p = nac_p;
    }

    public Aircraft withnac_p(Integer nacP) {
        this.nac_p = nacP;
        return this;
    }

    public Integer getnac_v() {
        return nac_v;
    }

    public void setnac_v(Integer nac_v) {
        this.nac_v = nac_v;
    }

    public Aircraft withnac_v(Integer nacV) {
        this.nac_v = nacV;
        return this;
    }

    public Integer getsil() {
        return sil;
    }

    public void setsil(Integer sil) {
        this.sil = sil;
    }

    public Aircraft withsil(Integer sil) {
        this.sil = sil;
        return this;
    }

    public String getsil_type() {
        return sil_type;
    }

    public void setsil_type(String sil_type) {
        this.sil_type = sil_type;
    }

    public Aircraft withsil_type(String silType) {
        this.sil_type = silType;
        return this;
    }

    public Integer getgva() {
        return gva;
    }

    public void setgva(Integer gva) {
        this.gva = gva;
    }

    public Aircraft withgva(Integer gva) {
        this.gva = gva;
        return this;
    }

    public Integer getsda() {
        return sda;
    }

    public void setsda(Integer sda) {
        this.sda = sda;
    }

    public Aircraft withsda(Integer sda) {
        this.sda = sda;
        return this;
    }

    public Integer getalert() {
        return alert;
    }

    public void setalert(Integer alert) {
        this.alert = alert;
    }

    public Aircraft withalert(Integer alert) {
        this.alert = alert;
        return this;
    }

    public Integer getspi() {
        return spi;
    }

    public void setspi(Integer spi) {
        this.spi = spi;
    }

    public Aircraft withspi(Integer spi) {
        this.spi = spi;
        return this;
    }

    public List<Object> getmlat() {
        return mlat;
    }

    public void setmlat(List<Object> mlat) {
        this.mlat = mlat;
    }

    public Aircraft withmlat(List<Object> mlat) {
        this.mlat = mlat;
        return this;
    }

    public List<Object> gettisb() {
        return tisb;
    }

    public void settisb(List<Object> tisb) {
        this.tisb = tisb;
    }

    public Aircraft withtisb(List<Object> tisb) {
        this.tisb = tisb;
        return this;
    }

    public Integer getmessages() {
        return messages;
    }

    public void setmessages(Integer messages) {
        this.messages = messages;
    }

    public Aircraft withmessages(Integer messages) {
        this.messages = messages;
        return this;
    }

    public Long getseen() {
        return seen;
    }

    public void setseen(Long seen) {
        this.seen = seen;
    }

    public Aircraft withseen(Long seen) {
        this.seen = seen;
        return this;
    }

    public Double getrssi() {
        return rssi;
    }

    public void setrssi(Double rssi) {
        this.rssi = rssi;
    }

    public Aircraft withrssi(Double rssi) {
        this.rssi = rssi;
        return this;
    }

    public Double getdst() {
        return dst;
    }

    public void setdst(Double dst) {
        this.dst = dst;
    }

    public Aircraft withdst(Double dst) {
        this.dst = dst;
        return this;
    }

    public Double getdir() {
        return dir;
    }

    public void setdir(Double dir) {
        this.dir = dir;
    }

    public Aircraft withdir(Double dir) {
        this.dir = dir;
        return this;
    }

    public String getairport() {
        return airport;
    }

    public void setairport(String airport) {
        this.airport = airport;
    }

    public VerticalDirection getverticaldirection() {
        return verticalDirection;
    }

    public void setverticaldirection(VerticalDirection verticalDirection) {
        this.verticalDirection = verticalDirection;
    }

    @Override
    public JsonObject toJson() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fromJson(JsonObject json) {
        now = asLong(json.get("now"));
        hex = asString(json.get("hex"));
        type = asString(json.get("type"));
        flight = asString(json.get("flight"));
        r = asString(json.get("r"));
        t = asString(json.get("t"));

        try {
            if (json.get("alt_baro") != null && json.get("alt_baro").isString() && asString(json.get("alt_baro")).toLowerCase().equals("ground")) {
                alt_baro = 0;
            } else {
                alt_baro = asInt(json.get("alt_baro"));
            }
        } catch (Exception e) {
            System.out.println("Unable to decode alt_baro :" + e.getMessage());
        }

        try {
            if (json.get("alt_geom") != null && json.get("alt_geom").isString() && asString(json.get("alt_geom")).toLowerCase().equals("ground")) {
                alt_geom = 0;
            } else {
                alt_geom = asInt(json.get("alt_geom"));
            }
        } catch (Exception e) {
            System.out.println("Unable to decode alt_geom :" + e.getMessage());
        }
        alt = asLong(json.get("alt"));
        gs = asDouble(json.get("gs"));
        ias = asInt(json.get("ias"));
        tas = asInt(json.get("tas"));
        mach = asDouble(json.get("mach"));
        wd = asInt(json.get("wd"));
        ws = asInt(json.get("ws"));
        oat = asInt(json.get("oat"));
        tat = asInt(json.get("tat"));
        track = asDouble(json.get("track"));
        track_rate = asDouble(json.get("track_rate"));
        roll = asDouble(json.get("roll"));
        mag_heading = asDouble(json.get("mag_heading"));
        true_heading = asDouble(json.get("true_heading"));
        baro_rate = asInt(json.get("baro_rate"));
        geom_rate = asInt(json.get("geom_rate"));
        squawk = asString(json.get("squawk"));
        emergency = asString(json.get("emergency"));
        category = asString(json.get("category"));

        nav_qnh = asDouble(json.get("nav_qnh"));
        nav_altitude_mcp = asInt(json.get("nav_altitude_mcp"));
        nav_altitude_fms = asInt(json.get("nav_altitude_fms"));
        nav_heading = asDouble(json.get("nav_heading"));
        nav_modes = asStringArray(json.get("nav_modes"));

        lat = asDouble(json.get("lat"));
        lon = asDouble(json.get("lon"));
        rr_lat = asDouble(json.get("rr_lat"));
        rr_lon = asDouble(json.get("rr_lon"));

        nic = asInt(json.get("nic"));
        rc = asInt(json.get("rc"));
        seen_pos = asDouble(json.get("seen_pos"));
        version = asInt(json.get("version"));
        nic_baro = asInt(json.get("nic_baro"));
        nac_p = asInt(json.get("nac_p"));
        nac_v = asInt(json.get("nac_v"));
        sil = asInt(json.get("sil"));
        sil_type = asString(json.get("sil_type"));
        gva = asInt(json.get("gva"));
        sda = asInt(json.get("sda"));
        alert = asInt(json.get("alert"));
        spi = asInt(json.get("spi"));
        messages = asInt(json.get("messages"));
        
        seen = asLong(json.get("seen"));
        rssi = asDouble(json.get("rssi"));
        dst = asDouble(json.get("dst"));
        dir = asDouble(json.get("dir"));

        pos_time = asLong(json.get("posTime"));
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                "reg='" + r + '\'' +
                ", flight='" + flight + '\'' +
                ", alt_baro=" + alt_baro +
                ", alt_geom=" + alt_geom +
                ", time=" + Util.toLocalDateTime(getpos_time()).toLocalTime() +
                ", coord=" + String.format("%.2f,%.2f", lat, lon) +
                ", type='" + t + '\'' +
                ", callsign=" + flight + '\'' +
                ", airport='" + airport + '\'' +
                '}';
    }

    public String fullString() {
        return "Aircraft{" +
                "now=" + now +
                ", hex='" + hex + '\'' +
                ", type='" + type + '\'' +
                ", flight='" + flight + '\'' +
                ", r='" + r + '\'' +
                ", t='" + t + '\'' +
                ", alt_baro=" + alt_baro +
                ", alt_geom=" + alt_geom +
                ", gs=" + gs +
                ", ias=" + ias +
                ", tas=" + tas +
                ", mach=" + mach +
                ", wd=" + wd +
                ", ws=" + ws +
                ", oat=" + oat +
                ", pos_time= " + getpos_time() +
                ", tat=" + tat +
                ", track=" + track +
                ", track_rate=" + track_rate +
                ", roll=" + roll +
                ", mag_heading=" + mag_heading +
                ", true_heading=" + true_heading +
                ", baro_rate=" + baro_rate +
                ", geom_rate=" + geom_rate +
                ", squawk='" + squawk + '\'' +
                ", emergency='" + emergency + '\'' +
                ", category='" + category + '\'' +
                ", nav_qnh=" + nav_qnh +
                ", nav_altitude_mcp=" + nav_altitude_mcp +
                ", nav_altitude_fms=" + nav_altitude_fms +
                ", nav_heading=" + nav_heading +
                ", nav_modes='" + nav_modes + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", rr_lat=" + rr_lat +
                ", rr_lon=" + rr_lon +
                ", nic=" + nic +
                ", rc=" + rc +
                ", seen_pos=" + seen_pos +
                ", version=" + version +
                ", nic_baro=" + nic_baro +
                ", nac_p=" + nac_p +
                ", nac_v=" + nac_v +
                ", sil=" + sil +
                ", sil_type='" + sil_type + '\'' +
                ", gva=" + gva +
                ", sda=" + sda +
                ", alert=" + alert +
                ", spi=" + spi +
                ", mlat=" + mlat +
                ", tisb=" + tisb +
                ", messages=" + messages +
                ", seen=" + seen +
                ", rssi=" + rssi +
                ", dst=" + dst +
                ", dir=" + dir +
                ", airport='" + airport + '\'' +
                ", vertical_direction=" + verticalDirection +
                '}';
    }
}
