import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-current-document-mark',
  templateUrl: './current-document-mark.component.html',
  styleUrls: ['./current-document-mark.component.css']
})
export class CurrentDocumentMarkComponent implements OnInit {
  @Input() date:string;
  constructor() { }

  ngOnInit() {
  }

}
