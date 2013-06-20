describe("SignupTermSort", function() {

    it("should work on empty terms", function() {
        var terms = [""];
        Signup.term.sortArray(terms);
        expect(terms).toEqual([""]);
    });

    it("should sort terms within years", function() {
        var terms = ["Trinity 2012",  "Hilary 2012", "Michaelmas 2012"];
        Signup.term.sortArray(terms);
        expect(terms).toEqual([ "Michaelmas 2012", "Trinity 2012", "Hilary 2012" ]);
    });

    it("should cope with bad data", function() {
        var terms = [ "Bad", "Data"];
        expect(function() {Signup.term.sortArray(terms)}).not.toThrow();
    });

    it("should sort across years", function() {
        var terms = ["Hilary 2010", "Hilary 2011", "Hilary 2013"];
        Signup.term.sortArray(terms);
        expect(terms).toEqual(["Hilary 2013", "Hilary 2011", "Hilary 2010"]);
    });

    it("sort data across years", function() {
        var terms = ["Hilary 2010", "Michaelmas 2010", "Hilary 2011", "Trinity 2011", "Trinity 2013", "Michaelmas 2013"];
        Signup.term.sortArray(terms);
        expect(terms).toEqual(["Michaelmas 2013", "Trinity 2013", "Trinity 2011", "Hilary 2011", "Michaelmas 2010"]);
    });


});