import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateDispDialogComponent } from './update-disp-dialog.component';

describe('UpdateDispDialogComponent', () => {
  let component: UpdateDispDialogComponent;
  let fixture: ComponentFixture<UpdateDispDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdateDispDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateDispDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
