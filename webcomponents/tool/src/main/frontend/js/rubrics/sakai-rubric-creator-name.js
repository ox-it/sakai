import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricCreatorName extends RubricsElement {

  constructor() {

    super();
    this.rubricId = ""
    this.creatorId = "";
    this.creatorName = "";
    this.token = ""
  }

  static get properties() {
    return {
      rubricId: {attribute: "rubric-id", type: String},
      creatorId: {attribute: "creator-id", type: String},
      creatorName: {type: String},
      token: {type: String}
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "rubric-id") {
        this.rubricId = newValue;
    } else if (name === "creator-id") {
        this.creatorId = newValue;
    } else if (name === "token") {
        this.token = newValue;
    }

    if (this.rubricId && this.creatorId && this.token) {
      this.setCreatorName();
    }
  }

  render() {
    return html`${this.creatorName}`;
  }

  setCreatorName() {

    var self = this;
    jQuery.ajax({
      url: `/rubrics-service/getCreatorDisplayNameForRubric?rubricId=${this.rubricId}`,
      headers: {"authorization": this.token},
      contentType: "application/json"
    }).done(function (response) {
      self.creatorName = response;
    }).fail(function () {
      self.creatorName = self.creatorId;
    });
  }
}

customElements.define("sakai-rubric-creator-name", SakaiRubricCreatorName);
