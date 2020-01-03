import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {Point} from 'geojson';

@Component({
  selector: 'app-stops-map',
  templateUrl: './stops-map.component.html',
  styleUrls: ['./stops-map.component.css']
})
export class StopsMapComponent implements OnInit {

  markerGreen = '../../../../assets/png/marker_green.png';
  markerBlue = '../../../../assets/png/marker_blue.png';
  @Input() center: Point;
  @Input() stops: Point[];
  @Input() description: string[];
  @Output() changeStop = new EventEmitter<number>();

  constructor() {
  }

  changeStopFun(index: number) {
    this.changeStop.emit();
  }

  ngOnInit() {
  }

}
