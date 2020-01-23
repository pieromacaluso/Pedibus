import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StopsMapComponent } from './stops-map.component';

describe('StopMapComponent', () => {
  let component: StopsMapComponent;
  let fixture: ComponentFixture<StopsMapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StopsMapComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StopsMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
