import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'fortmatLine'
})
export class FortmatLinePipe implements PipeTransform {

  transform(lineaName: string, args?: any): any {
    const toUpper = lineaName.charAt(0).toUpperCase() + lineaName.substr(1, lineaName.length);
    return [toUpper.substr(0, 5), ' ', toUpper.substr(5, toUpper.length)].join('');
  }

}
