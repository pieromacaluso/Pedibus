import {Component, Input, OnInit} from '@angular/core';

/**
 * Componente che visualizza la mappa per una singola fermata
 */
@Component({
  selector: 'app-stop-map',
  templateUrl: './stop-map.component.html',
  styleUrls: ['./stop-map.component.css']
})
export class StopMapComponent implements OnInit {

  @Input() lat: number;
  @Input() lng: number;
  @Input() description: string;
  constructor() { }

  ngOnInit() {
  }

}
