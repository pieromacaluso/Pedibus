import {
  trigger,
  transition,
  style,
  query,
  group,
  animateChild,
  animate,
  keyframes,
} from '@angular/animations';

export const fadeAnimation =
  trigger('fadeAnimation', [
    transition('* => *', [
      query(':enter',
        [
          style({
            position: 'absolute',
            left: 0,
            width: '100%',
            opacity: 0,
            transform: 'scale(0) translateY(100%)'
          })
        ],
        {optional: true}
      ),
      query(':leave',
        [
          animate('0.2s ease-out', style({opacity: 0, height: 0}))
        ],
        {optional: true}
      ),
      query(':leave',
        [
          animate('0.0s', style({transform: 'scale(0) translateY(100%)'}))
        ],
        {optional: true}
      ),
      query(':enter',
        [
          animate('0.0s', style({transform: 'scale(1) translateY(0)'}))
        ],
        {optional: true}
      ),
      query(':enter',
        [
          animate('0.2s ease-in', style({opacity: 1}))
        ],
        {optional: true}
      )
    ])
  ]);
