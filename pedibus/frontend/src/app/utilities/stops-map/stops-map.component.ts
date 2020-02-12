import {Component, Directive, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild} from '@angular/core';
import {Point} from 'geojson';
import {AgmMap} from '@agm/core';
import {StopsByLine} from '../../logged/line-details';

/**
 * Componente che visualizza la mappa per un'intera linea
 */
@Component({
  selector: 'app-stops-map',
  templateUrl: './stops-map.component.html',
  styleUrls: ['./stops-map.component.css']
})
export class StopsMapComponent implements OnInit, OnChanges {

  markerGreen = '../../../../assets/png/marker_green.png';
  markerBlue = '../../../../assets/png/marker_blue.png';
  @Input() center: Point;
  @Input() stops: Point[];
  @Input() lines: Map<string, StopsByLine>;
  @Input() description: string[];
  @Output() changeStop = new EventEmitter<number>();
  @ViewChild('agmMap', {static: false}) agmMap: AgmMap;

  constructor() {
  }

  ngOnChanges() {
    setTimeout(() => {
      this.agmMap.triggerResize();
    }, 500);
  }

  changeStopFun(index: number) {
    this.changeStop.emit();
  }

  ngOnInit() {

  }

  recenter(map: AgmMap) {
    map.boundsChange.emit();
  }
}
