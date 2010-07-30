describe("Text", function() {

	it("should markup and email", function() {
		var html = Text.toHtml("test@example.com");
		expect(html).toContain("mailto:test@example.com");
	});
	
	it("should ignore @s", function() {
		var html = Text.toHtml("Prices are @ 10 pounds a place.");
		expect(html).not.toContain("mailto:");
	});
	
	it("should markup links", function() {
		var html = Text.toHtml("More information can be found at http://www.example.com/newsite");
		expect(html).toContain('href="http://www.example.com/newsite"');
	});
	
	it("should split on newlines", function() {
		var html = Text.toHtml("Line 1\nLine 2\n");
		expect(html).toContain('<br>');
	});
	
	it("should escape multiple copies", function() {
		var html = Text.toHtml("This is all true 1 < 5 < 8 > -1");
		expect(html).not.toContain('<');
	});
});