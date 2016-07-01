// Title: Tigra Calendar
// URL: http://www.softcomplex.com/products/tigra_calendar/
// Version: 3.2 (American date format)
// Date: 10/14/2002 (mm/dd/yyyy)
// Note: Permission given to use this script in ANY kind of applications if
//    header lines are left unchanged.
// Note: Script consists of two files: calendar?.js and calendar.html

// borrowed from Melete 2.4, slightly changed -ggolden

// if two digit year input dates after this year considered 20 century.
var NUM_CENTYEAR = 30;
// is time input control required by default
var BUL_TIMECOMPONENT = true;
// are year scrolling buttons required by default
var BUL_YEARSCROLL = true;

var calendars = [];
var RE_NUM = /^\-?\d+$/;

function calendar2(obj_target,earlyLate) {

	// assigning methods
	this.gen_date = ambrosia_format_date; //cal_gen_date2;
	this.gen_time = ambrosia_format_time; // cal_gen_time2;
	this.gen_tsmp = cal_gen_tsmp2;
	this.prs_date = ambrosia_parse_date; // cal_prs_date2;
	this.prs_time = ambrosia_parse_time; // cal_prs_time2;
	this.prs_tsmp = ambrosia_parse_timeStamp; // cal_prs_tsmp2;
	this.prs_ampm = ambrosia_parse_am_pm; // cal_prs_ampm2;
	this.popup    = cal_popup2;
	this.month_names = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

	if (earlyLate == 0)
		this.gen_now = ambrosia_now_early;
	else
		this.gen_now = ambrosia_now_late;

	// validate input parameters
	if (!obj_target)
		return cal_error("Error calling the calendar: no target control specified");
	if (obj_target.value == null)
		return cal_error("Error calling the calendar: parameter specified is not valid target control");
	this.target = obj_target;
	this.time_comp = BUL_TIMECOMPONENT;
	this.year_scroll = BUL_YEARSCROLL;
	
	// register in global collections
	this.id = calendars.length;
	calendars[this.id] = this;
	
	this.targetValue = this.target.value;
	if (this.targetValue.length == 0)
	{
		var d = new Date();
		var amPm = "AM";
		if (earlyLate == 0)
		{
			d.setHours(8);
			d.setMinutes(0);
		}
		else
		{
			d.setHours(11);
			d.setMinutes(59);
			amPm = "PM";
		}
		d.setSeconds(0);
		d.setMilliseconds(0);
		
		var sd = this.gen_date(d);
		var st = this.gen_time(d);
		var ss = sd + " " + st + " " + amPm;
		this.targetValue = ss;
	}
}

function ambrosia_now_early()
{
	var rv = new Date();
	rv.setHours(8);
	rv.setMinutes(0);
	rv.setSeconds(0);
	rv.setMilliseconds(0);
	return rv;
}

function ambrosia_now_late()
{
	var rv = new Date();
	rv.setHours(23);
	rv.setMinutes(59);
	rv.setSeconds(0);
	rv.setMilliseconds(0);
	return rv;
}

function cal_popup2 (str_datetime, ampm_val) {

	this.dt_current = this.prs_tsmp(str_datetime ? str_datetime : this.targetValue);
	if (!this.dt_current) return;
	
	if (!str_datetime)
	{
	  this.ampm_val = this.prs_ampm(this.targetValue);
	}
	else
	{
	  this.ampm_val = ampm_val;
	} 

	var obj_calwindow = window.open(
		'/ambrosia_library/calendar/calendar.html?datetime=' + this.dt_current.valueOf()+ '&ampmval=' + this.ampm_val +
		'&id=' + this.id,
		'Calendar', 'width=250,height='+(this.time_comp ? 250 : 190)+
		',status=no,resizable=no,top=200,left=200,dependent=yes,alwaysRaised=yes'
	);
	obj_calwindow.opener = window;
	obj_calwindow.focus();
}

// timestamp generating function
function cal_gen_tsmp2 (dt_datetime) {
return(this.gen_date(dt_datetime) + ' ' + this.gen_time(dt_datetime));

}

function ambrosia_format_date(timeStamp)
{
	var rv = this.month_names[timeStamp.getMonth()];
	rv += " ";
	rv += timeStamp.getDate();
	rv += ", ";
	rv += timeStamp.getFullYear();
	return rv;
}

// date generating function
function cal_gen_date2 (dt_datetime) {
	return (
		(dt_datetime.getMonth() < 9 ? '0' : '') + (dt_datetime.getMonth() + 1) + "/"
		+ (dt_datetime.getDate() < 10 ? '0' : '') + dt_datetime.getDate() + "/"
		+ dt_datetime.getFullYear()
	);
}

function ambrosia_format_time(timeStamp)
{
	var hours = timeStamp.getHours();
	//if (hours == 0) hours = 12;
	var rv = hours + ":";
	rv += (timeStamp.getMinutes() < 10 ? "0" : "") + timeStamp.getMinutes();
	return rv;
}

// time generating function
function cal_gen_time2 (dt_datetime) {
	return (
		(dt_datetime.getHours() < 10 ? '0' : '') + dt_datetime.getHours() + ":"
		+ (dt_datetime.getMinutes() < 10 ? '0' : '') + (dt_datetime.getMinutes()) 
		//+ ":"
		//+ (dt_datetime.getSeconds() < 10 ? '0' : '') + (dt_datetime.getSeconds())
	);
}

function ambrosia_parse_timeStamp(displayStr)
{
	if (displayStr == null) return this.gen_now();

	var time = parseInt(displayStr, 10);
	if (!isNaN(time) && (time >= 0)) return new Date(time);

	var displayParts = displayStr.split(" ");
	if (displayParts.length != 5) this.gen_now();

	var datePart = displayParts[0] + " " + displayParts[1] + " " + displayParts[2];
	var timePart = displayParts[3] + " " + displayParts[4];
	return this.prs_time(timePart, this.prs_date(datePart));
}

// timestamp parsing function
function cal_prs_tsmp2 (str_datetime) {
	// if no parameter specified return current timestamp
	if (!str_datetime)
		return (new Date());

	// if positive integer treat as milliseconds from epoch
	if (RE_NUM.exec(str_datetime))
		return new Date(str_datetime);
		
	// else treat as date in string format
	var arr_datetime = str_datetime.split(' ');

	//return this.prs_time(arr_datetime[1], this.prs_date(arr_datetime[0]))+' '+arr_datetime[2];
	return this.prs_time(arr_datetime[1], this.prs_date(arr_datetime[0]));
}

function ambrosia_parse_am_pm(displayStr)
{
	if (displayStr == null) return this.gen_now();

	var time = parseInt(displayStr, 10);
	if (!isNaN(time) && (time >= 0)) return new Date(time);
	
	var displayParts = displayStr.split(" ");
	return displayParts[displayParts.length-1];
}

// ampm parsing function
function cal_prs_ampm2 (str_datetime) {

	// if no parameter specified return current timestamp
	if (!str_datetime)
		return (this.gen_now());

	// if positive integer treat as milliseconds from epoch
	if (RE_NUM.exec(str_datetime))
		return new Date(str_datetime);
		
	// else treat as date in string format
	
	var arr_datetime = str_datetime.split(' ');
	//return this.prs_time(arr_datetime[1], this.prs_date(arr_datetime[0]))+' '+arr_datetime[2];
	return arr_datetime[2];
}

function ambrosia_parse_date(displayStr)
{
	var displayParts = displayStr.split(" ");
	//var rv = this.gen_now();
	var rv = new Date();
	if (displayParts.length == 3)
	{
		var month = -1;
		for (var i = 0; i <= 11; i++)
		{
			if (this.month_names[i].toLowerCase() == displayParts[0].toLowerCase())
			{
				month = i;
				break;
			}
		}
		if (month == -1) return rv;

		var day = parseInt(displayParts[1], 10);
		if (isNaN(day)) return rv;
		
		var year = parseInt(displayParts[2], 10);
		if (isNaN(year)) return rv;

		//rv.setYear(year);
		//rv.setMonth(month);
		//rv.setDate(day);
		rv.setFullYear(year,month,day);
	}

	return rv;
}

// date parsing function
function cal_prs_date2 (str_date) {

	var arr_date = str_date.split('/');

	if (arr_date.length != 3) return //alert ("Invalid date format: '" + str_date + "'.\nFormat accepted is dd-mm-yyyy.");
	if (!arr_date[1]) return //alert ("Invalid date format: '" + str_date + "'.\nNo day of month value can be found.");
	if (!RE_NUM.exec(arr_date[1])) return //alert ("Invalid day of month value: '" + arr_date[1] + "'.\nAllowed values are unsigned integers.");
	if (!arr_date[0]) return //alert ("Invalid date format: '" + str_date + "'.\nNo month value can be found.");
	if (!RE_NUM.exec(arr_date[0])) return //alert ("Invalid month value: '" + arr_date[0] + "'.\nAllowed values are unsigned integers.");
	if (!arr_date[2]) return //alert ("Invalid date format: '" + str_date + "'.\nNo year value can be found.");
	if (!RE_NUM.exec(arr_date[2])) return //alert ("Invalid year value: '" + arr_date[2] + "'.\nAllowed values are unsigned integers.");

	var dt_date = new Date();
	dt_date.setDate(1);

	if (arr_date[0] < 1 || arr_date[0] > 12) return //alert ("Invalid month value: '" + arr_date[0] + "'.\nAllowed range is 01-12.");
	dt_date.setMonth(arr_date[0]-1);
	 
	if (arr_date[2] < 100) arr_date[2] = Number(arr_date[2]) + (arr_date[2] < NUM_CENTYEAR ? 2000 : 1900);
	dt_date.setFullYear(arr_date[2]);

	var dt_numdays = new Date(arr_date[2], arr_date[0], 0);
	dt_date.setDate(arr_date[1]);
	if (dt_date.getMonth() != (arr_date[0]-1)) return //alert ("Invalid day of month value: '" + arr_date[1] + "'.\nAllowed range is 01-"+dt_numdays.getDate()+".");

	return (dt_date)
}

function ambrosia_parse_time(displayStr, timeStamp)
{
	var displayParts = displayStr.split(":");
	if ((displayParts.length == 2) || (displayParts.length == 3))
	{
		var hour = parseInt(displayParts[0], 10);
		if (isNaN(hour)) return timeStamp;
		if ((hour < 0) || (hour > 12)) return timeStamp;
		//if (hour == 12) hour = 0;
		
		var minute = parseInt(displayParts[1], 10);
		if (isNaN(minute)) return timeStamp;
		if ((minute < 0) || (minute > 59)) return timeStamp;
		
		timeStamp.setHours(hour);
		timeStamp.setMinutes(minute);
		timeStamp.setSeconds(0);
		timeStamp.setMilliseconds(0);		
	}
	
	return timeStamp;
}

// time parsing function
function cal_prs_time2 (str_time, dt_date) {

	if (!dt_date) return null;
	var arr_time = String(str_time ? str_time : '').split(':');
        
	if (!arr_time[0]) dt_date.setHours(0);
	else if (RE_NUM.exec(arr_time[0])) 
		if (arr_time[0] < 24) 
			dt_date.setHours(arr_time[0]);
		else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed range is 00-23.");
	else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed values are unsigned integers.");
	
	if (!arr_time[1]) dt_date.setMinutes(0);
	else if (RE_NUM.exec(arr_time[1]))
		if (arr_time[1] < 60) dt_date.setMinutes(arr_time[1]);
		else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed range is 00-59.");
	else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed values are unsigned integers.");
	/*
	if (!arr_time[2]) dt_date.setSeconds(0);
	else if (RE_NUM.exec(arr_time[2]))
		if (arr_time[2] < 60) dt_date.setSeconds(arr_time[2]);
		else return cal_error ("Invalid seconds value: '" + arr_time[2] + "'.\nAllowed range is 00-59.");
	else return cal_error ("Invalid seconds value: '" + arr_time[2] + "'.\nAllowed values are unsigned integers.");

	dt_date.setMilliseconds(0);
	*/
	
	return dt_date;
}

function cal_error (str_message) {
	//alert (str_message);
	return null;
}
