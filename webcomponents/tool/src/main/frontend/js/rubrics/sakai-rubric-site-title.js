import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricSiteTitle extends RubricsElement {

  constructor() {

    super();
    this.rubricId = "";
    this.siteId = "";
    this.siteTitle = "";
    this.token = "";
  }

  static get properties() {
    return {
        rubricId: {attribute: "rubric-id", type: String},
        siteId: {attribute: "site-id", type: String},
        siteTitle: {type: String},
        token: {type: String}
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "rubric-id") {
        this.rubricId = newValue;
    } else if (name === "site-id") {
        this.siteId = newValue;
    } else if (name === "token") {
        this.token = newValue;
    }

    if (this.rubricId && this.siteId && this.token) {
      this.setSiteTitle();
    }
  }

  render() {
    return html`${this.siteTitle}`;
  }

  setSiteTitle() {

    var self = this;
    jQuery.ajax({
      url: `/rubrics-service/getSiteTitleForRubric?rubricId=${this.rubricId}`,
      headers: {"authorization": this.token},
      contentType: "application/json"
    }).done(function (response) {
      self.siteTitle = response;
    }).fail(function () {
      self.siteTitle = self.siteId;
    });
  }
}

customElements.define("sakai-rubric-site-title", SakaiRubricSiteTitle);
