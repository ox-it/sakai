describe("SignupParseDate", function() {
  it("should parse good date", function() {
    var string = "2013-03-01T00:00:00Z";
    var d = Signup.util.parseDate(string);
    expect(d).toEqual(new Date(Date.UTC(2013, 02, 01, 0, 0, 0)));
  });

  it("should return bad date on empty date", function() {
    var string = "";
    var d = Signup.util.parseDate(string);
    expect(isNaN(d.getDate())).toBe(true);
  });

  it("should return bad date on null", function() {
    var string = null;
    var d = Signup.util.parseDate(string);
    expect(isNaN(d.getDate())).toBe(true);
  });

  it("should return bad date on badly formatted string", function() {
    var string = "this is not a date";
    var d = Signup.util.parseDate(string);
    expect(isNaN(d.getDate())).toBe(true);
  })
});
